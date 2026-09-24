#!/usr/bin/env bash
# Scan changed model_card, approval, deployment, and dataset_manifest files.
# Prints scan=skip when none of those paths changed. Writes SARIF from the
# evgraph CLI when one of them did. Does not call the release gate.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SARIF_OUT="${SARIF_OUT:-$ROOT/evgraph-artifacts.sarif}"

find_evgraph() {
  if [[ -n "${EVGRAPH_BIN:-}" && -x "${EVGRAPH_BIN}" ]]; then
    echo "$EVGRAPH_BIN"
    return
  fi
  if command -v evgraph >/dev/null 2>&1; then
    command -v evgraph
    return
  fi
  local sibling="$ROOT/../evgraph/.venv/bin/evgraph"
  if [[ -x "$sibling" ]]; then
    echo "$sibling"
    return
  fi
  echo "evgraph CLI not found. pip install evgraph, or set EVGRAPH_BIN." >&2
  exit 3
}

if [[ -n "${CHANGED_FILES+x}" ]]; then
  changed="$CHANGED_FILES"
elif [[ -n "${GITHUB_BASE_REF:-}" ]]; then
  changed="$(git -C "$ROOT" diff --name-only "origin/${GITHUB_BASE_REF}...HEAD")"
else
  changed=""
fi

matches=()
while IFS= read -r file; do
  [[ -z "$file" ]] && continue
  case "$(basename "$file")" in
    model_card.json|approval.json|deployment.json|dataset_manifest.csv)
      matches+=("$file")
      ;;
  esac
done <<<"$changed"

if [[ "${#matches[@]}" -eq 0 ]]; then
  echo "scan=skip"
  exit 0
fi

EVGRAPH="$(find_evgraph)"
parts=()
seen_dirs="|"

scan_promotion() {
  local dir="$1"
  local card="$ROOT/$dir/model_card.json"
  local approval="$ROOT/$dir/approval.json"
  local deployment="$ROOT/$dir/deployment.json"
  local part="$SARIF_OUT.part.${#parts[@]}"
  [[ -f "$card" && -f "$approval" && -f "$deployment" ]] || {
    echo "ci-artifact-scan: promotion set incomplete in $dir" >&2
    exit 1
  }
  "$EVGRAPH" scan-promotion \
    --model-card "$card" \
    --approval "$approval" \
    --deployment "$deployment" \
    --format sarif >"$part"
  parts+=("$part")
}

for file in "${matches[@]}"; do
  rel="${file#./}"
  base="$(basename "$rel")"
  dir="$(dirname "$rel")"
  if [[ "$base" == "dataset_manifest.csv" ]]; then
    part="$SARIF_OUT.part.${#parts[@]}"
    [[ -f "$ROOT/$rel" ]] || {
      echo "ci-artifact-scan: missing $rel" >&2
      exit 1
    }
    "$EVGRAPH" scan-dataset-manifest "$ROOT/$rel" --format sarif >"$part"
    parts+=("$part")
    continue
  fi
  case "$seen_dirs" in
    *"|$dir|"*) continue ;;
  esac
  seen_dirs="${seen_dirs}${dir}|"
  scan_promotion "$dir"
done

python3 - "$SARIF_OUT" "${parts[@]}" <<'PY'
import json, sys
dest, *paths = sys.argv[1:]
logs = [json.load(open(path)) for path in paths]
merged = {
    "$schema": logs[0].get("$schema"),
    "version": logs[0]["version"],
    "runs": [run for log in logs for run in log.get("runs", [])],
}
with open(dest, "w") as handle:
    json.dump(merged, handle, indent=2)
    handle.write("\n")
PY
rm -f "${parts[@]}"

echo "scan=run"
