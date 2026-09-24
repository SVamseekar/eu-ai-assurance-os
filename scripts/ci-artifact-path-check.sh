#!/usr/bin/env bash
# Local proof of the artifact-scan path filter. GitHub Actions does not run here.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SCAN="$ROOT/scripts/ci-artifact-scan.sh"
GATE="$ROOT/scripts/ci-release-gate.sh"
PACK="services/api/src/main/resources/public-claims/getsafe"
REAL_EVGRAPH="$ROOT/../evgraph/.venv/bin/evgraph"

fail() {
  echo "ci-artifact-path-check: $*" >&2
  exit 1
}

[[ -x "$REAL_EVGRAPH" ]] || fail "evgraph CLI missing at $REAL_EVGRAPH"

tmpdir="$(mktemp -d)"
trap 'rm -rf "$tmpdir"' EXIT

stub="$tmpdir/evgraph-stub"
cat >"$stub" <<'EOF'
#!/usr/bin/env bash
echo "stub-invoked" >>"${STUB_LOG:?}"
echo '{"version":"0.0.0","runs":[]}'
EOF
chmod +x "$stub"

# A README-only change must not invoke evgraph and must not write SARIF.
readme_sarif="$tmpdir/readme.sarif"
readme_log="$tmpdir/stub.log"
: >"$readme_log"
set +e
readme_out="$(
  STUB_LOG="$readme_log" \
  EVGRAPH_BIN="$stub" \
  CHANGED_FILES=$'README.md\napps/dashboard/README.md\ndocs/guide/README.md' \
  SARIF_OUT="$readme_sarif" \
  bash "$SCAN" 2>"$tmpdir/readme.err"
)"
readme_code=$?
set -e
[[ "$readme_code" -eq 0 ]] || fail "README change exited $readme_code"
[[ "$readme_out" == *"scan=skip"* ]] || fail "README change did not skip: $readme_out"
[[ ! -e "$readme_sarif" ]] || fail "README change wrote SARIF"
[[ ! -s "$readme_log" ]] || fail "README change invoked evgraph"

# A model_card.json change must run the real CLI, then the release-gate script.
card_sarif="$tmpdir/card.sarif"
set +e
card_out="$(
  env -u EVGRAPH_BIN \
  CHANGED_FILES="$PACK/model_card.json" \
  SARIF_OUT="$card_sarif" \
  bash "$SCAN" 2>"$tmpdir/card.err"
)"
card_code=$?
set -e
[[ "$card_code" -eq 0 ]] || fail "model_card scan exited $card_code: $(cat "$tmpdir/card.err")"
[[ "$card_out" == *"scan=run"* ]] || fail "model_card change did not scan: $card_out"
python3 - "$card_sarif" <<'PY'
import json, sys
doc = json.load(open(sys.argv[1]))
assert doc["version"] == "2.1.0", doc.get("version")
assert doc["runs"][0]["tool"]["driver"]["name"] == "evgraph"
assert "results" in doc["runs"][0]
PY

set +e
env -u ASSURANCE_API_BASE -u API_KEY -u ASSURANCE_API_KEY -u SYSTEM_ID -u FAIL_ON_REVIEW \
  bash "$GATE" >"$tmpdir/gate.out" 2>"$tmpdir/gate.err"
gate_code=$?
set -e
[[ "$gate_code" -eq 3 ]] || fail "release gate after model_card exited $gate_code, want 3 (usage)"
[[ -s "$card_sarif" ]] || fail "SARIF missing after the gate step"

# The same filter must scan approval.json, deployment.json, and the dataset CSV.
for rel in \
  "$PACK/approval.json" \
  "$PACK/deployment.json" \
  "$PACK/dataset_manifest.csv"
do
  out_sarif="$tmpdir/$(basename "$rel").sarif"
  set +e
  out="$(
    env -u EVGRAPH_BIN \
    CHANGED_FILES="$rel" \
    SARIF_OUT="$out_sarif" \
    bash "$SCAN" 2>"$tmpdir/one.err"
  )"
  code=$?
  set -e
  [[ "$code" -eq 0 ]] || fail "$rel scan exited $code: $(cat "$tmpdir/one.err")"
  [[ "$out" == *"scan=run"* ]] || fail "$rel did not scan: $out"
  python3 - "$out_sarif" <<'PY'
import json, sys
doc = json.load(open(sys.argv[1]))
assert doc["version"] == "2.1.0", doc.get("version")
assert doc["runs"][0]["tool"]["driver"]["name"] == "evgraph"
PY
done

workflow="$ROOT/.github/workflows/artifact-scan.yml"
[[ -f "$workflow" ]] || fail "missing $workflow"
python3 - "$workflow" "$ROOT/.github/workflows/release-gate-example.yml" <<'PY'
import sys
scan = open(sys.argv[1]).read()
sample = open(sys.argv[2]).read()
for needle in (
    "**/model_card.json",
    "**/approval.json",
    "**/deployment.json",
    "**/dataset_manifest.csv",
    "scripts/ci-artifact-scan.sh",
    "scripts/ci-release-gate.sh",
    "upload-sarif",
    "pull_request:",
):
    if needle not in scan:
        raise SystemExit(f"artifact-scan.yml missing {needle}")
if "FAIL_ON_REVIEW" in scan:
    raise SystemExit("artifact-scan.yml must leave FAIL_ON_REVIEW at the script default")
lower = scan.lower()
for banned in ("eu-ai-slm", "scorer", "break-glass", "break_glass", "qlora"):
    if banned in lower:
        raise SystemExit(f"artifact-scan.yml contains {banned}")
if scan.find("ci-artifact-scan.sh") > scan.find("ci-release-gate.sh"):
    raise SystemExit("release gate step is before the scan")
if scan.find("upload-sarif") > scan.find("ci-release-gate.sh"):
    raise SystemExit("SARIF upload is after the release gate")
if "pull_request:" in sample:
    raise SystemExit("release-gate-example.yml must stay off pull_request")
if "workflow_call:" not in sample or "workflow_dispatch:" not in sample:
    raise SystemExit("release-gate-example.yml lost workflow_call or workflow_dispatch")
PY

echo "ci-artifact-path-check: README skipped; model_card, approval, deployment, and dataset CSV scanned; gate script ran after the scan"
