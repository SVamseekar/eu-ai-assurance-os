#!/usr/bin/env bash
# Start local API (H2) + Next.js dashboard with Google OAuth from root .env
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ ! -f .env ]]; then
  echo "Missing .env — copy .env.example and set OAUTH_GOOGLE_CLIENT_ID / SECRET"
  echo "  cp .env.example .env"
  exit 1
fi

set -a
# shellcheck disable=SC1091
source .env
set +a

if [[ -z "${OAUTH_GOOGLE_CLIENT_ID:-}" || -z "${OAUTH_GOOGLE_CLIENT_SECRET:-}" ]]; then
  echo "OAUTH_GOOGLE_CLIENT_ID / OAUTH_GOOGLE_CLIENT_SECRET are empty in .env"
  echo "See docs/LOCAL_GOOGLE_OAUTH.md"
  exit 1
fi

export OAUTH_REDIRECT_BASE_URL="${OAUTH_REDIRECT_BASE_URL:-http://localhost:3000}"
export OAUTH_AUTO_PROVISION="${OAUTH_AUTO_PROVISION:-true}"
export ASSURANCE_API_BASE_URL="${ASSURANCE_API_BASE_URL:-http://localhost:8080}"
export NEXT_PUBLIC_SITE_URL="${NEXT_PUBLIC_SITE_URL:-http://localhost:3000}"
export EVAL_CALLBACK_SECRET="${EVAL_CALLBACK_SECRET:-local-dev-eval-callback-secret}"
export AUDIT_CHAIN_SECRET="${AUDIT_CHAIN_SECRET:-local-dev-audit-chain-secret}"

echo "Google OAuth client: ${OAUTH_GOOGLE_CLIENT_ID:0:24}…"
echo "Redirect base:       $OAUTH_REDIRECT_BASE_URL"
echo "Auto-provision:      $OAUTH_AUTO_PROVISION"
echo ""
echo "Required Google Cloud redirect URI (exact):"
echo "  ${OAUTH_REDIRECT_BASE_URL}/api/auth/oauth/google/callback"
echo ""

# Ensure dashboard local env
mkdir -p apps/dashboard
cat > apps/dashboard/.env.local <<EOF
ASSURANCE_API_BASE_URL=$ASSURANCE_API_BASE_URL
NEXT_PUBLIC_SITE_URL=$NEXT_PUBLIC_SITE_URL
EOF

cleanup() {
  if [[ -n "${API_PID:-}" ]]; then kill "$API_PID" 2>/dev/null || true; fi
  if [[ -n "${WEB_PID:-}" ]]; then kill "$WEB_PID" 2>/dev/null || true; fi
}
trap cleanup EXIT INT TERM

echo "Starting API on :8080…"
(
  cd services/api
  mvn -q spring-boot:run
) &
API_PID=$!

# Wait for API health
for i in $(seq 1 60); do
  if curl -sf http://localhost:8080/actuator/health >/dev/null 2>&1; then
    echo "API is up."
    break
  fi
  if ! kill -0 "$API_PID" 2>/dev/null; then
    echo "API process exited early"
    exit 1
  fi
  sleep 2
done

echo "Starting dashboard on :3000…"
(
  cd apps/dashboard
  npm run dev -- --port 3000
) &
WEB_PID=$!

echo ""
echo "Open http://localhost:3000/login → Continue with Google"
echo "Password login still works: compliance@example.com / (see BootstrapData / docs)"
wait
