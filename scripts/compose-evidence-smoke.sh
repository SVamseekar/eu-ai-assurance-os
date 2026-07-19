#!/usr/bin/env bash
# Smoke: login → ensure a system → create memory evidence → query with citations.
# Requires a running API (Compose or mvn spring-boot:run).
set -euo pipefail

API_BASE="${ASSURANCE_API_BASE:-http://localhost:8080}"
EMAIL="${SMOKE_EMAIL:-compliance@example.com}"
PASSWORD="${SMOKE_PASSWORD:-dev-local-password-only}"

echo "==> Health: $API_BASE/actuator/health"
curl -fsS "$API_BASE/actuator/health" | head -c 400
echo

echo "==> Login as $EMAIL"
LOGIN_JSON=$(curl -fsS -X POST "$API_BASE/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")

if command -v jq >/dev/null 2>&1; then
  TOKEN=$(echo "$LOGIN_JSON" | jq -r '.accessToken // .access_token // empty')
else
  TOKEN=$(python3 -c 'import json,sys; d=json.load(sys.stdin); print(d.get("accessToken") or d.get("access_token") or "")' <<<"$LOGIN_JSON")
fi

if [[ -z "${TOKEN:-}" || "$TOKEN" == "null" ]]; then
  echo "Login failed; response:" >&2
  echo "$LOGIN_JSON" >&2
  exit 1
fi

auth=(-H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json')

echo "==> List systems"
SYSTEMS=$(curl -fsS "${auth[@]}" "$API_BASE/api/v1/systems")
if command -v jq >/dev/null 2>&1; then
  SYSTEM_ID=$(echo "$SYSTEMS" | jq -r 'if type=="array" then .[0].id // empty else .items[0].id // empty end')
else
  SYSTEM_ID=$(python3 -c 'import json,sys
d=json.load(sys.stdin)
if isinstance(d,list) and d: print(d[0].get("id",""))
elif isinstance(d,dict) and d.get("items"): print(d["items"][0].get("id",""))
else: print("")' <<<"$SYSTEMS")
fi

if [[ -z "${SYSTEM_ID:-}" || "$SYSTEM_ID" == "null" ]]; then
  echo "==> No system found; registering smoke system"
  CREATE=$(curl -fsS -X POST "${auth[@]}" "$API_BASE/api/v1/systems" \
    -d '{
      "name": "Compose Smoke System",
      "owner": "Smoke Runner",
      "purpose": "Part 9 evidence smoke",
      "riskClass": "LIMITED",
      "riskBasis": "Local compose smoke test",
      "deploymentRegion": "EU",
      "evidenceCoverage": 0,
      "evalScore": 0,
      "dataContractStatus": "HEALTHY",
      "openGaps": []
    }')
  if command -v jq >/dev/null 2>&1; then
    SYSTEM_ID=$(echo "$CREATE" | jq -r '.id')
  else
    SYSTEM_ID=$(python3 -c 'import json,sys; print(json.load(sys.stdin).get("id",""))' <<<"$CREATE")
  fi
fi

echo "    systemId=$SYSTEM_ID"

echo "==> Ingest memory evidence document"
curl -fsS -X POST "${auth[@]}" "$API_BASE/api/v1/evidence/documents" \
  -d "$(cat <<EOF
{
  "systemId": "$SYSTEM_ID",
  "type": "SOP",
  "title": "Compose smoke human oversight SOP",
  "sourceUri": "memory://compose-smoke-oversight",
  "content": "Human oversight SOP for smoke test. Reviewers must approve high-risk releases. Contact compliance@example.com. This document supports EU AI Act-oriented governance gates."
}
EOF
)" | head -c 500
echo

echo "==> Query evidence (expect citations)"
QUERY=$(curl -fsS -X POST "${auth[@]}" "$API_BASE/api/v1/evidence/query" \
  -d "{\"systemId\":\"$SYSTEM_ID\",\"question\":\"What does the human oversight SOP require?\"}")

echo "$QUERY" | head -c 1200
echo

if command -v jq >/dev/null 2>&1; then
  CITES=$(echo "$QUERY" | jq -r '(.citations // .sources // []) | length')
  echo "==> citation count: ${CITES:-unknown}"
else
  echo "==> (install jq for citation count)"
fi

echo "==> Smoke OK"
