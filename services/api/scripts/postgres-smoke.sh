#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WORK_DIR="${TMPDIR:-/tmp}/eu-ai-assurance-postgres-smoke"
DATA_DIR="$WORK_DIR/data"
SOCKET_DIR="$WORK_DIR/socket"
LOG_FILE="$WORK_DIR/postgres.log"
PORT="${POSTGRES_SMOKE_PORT:-55432}"
DB_NAME="${POSTGRES_SMOKE_DB:-eu_ai_assurance_smoke}"
DB_USER="${POSTGRES_SMOKE_USER:-eu_ai_assurance}"

rm -rf "$WORK_DIR"
mkdir -p "$DATA_DIR" "$SOCKET_DIR"

cleanup() {
  if [[ -n "${POSTGRES_PID:-}" ]] && kill -0 "$POSTGRES_PID" 2>/dev/null; then
    kill "$POSTGRES_PID"
    wait "$POSTGRES_PID" 2>/dev/null || true
  fi
}
trap cleanup EXIT

initdb -D "$DATA_DIR" -A trust -U "$DB_USER" >/dev/null
postgres -D "$DATA_DIR" -k "$SOCKET_DIR" -p "$PORT" >"$LOG_FILE" 2>&1 &
POSTGRES_PID="$!"

for _ in {1..60}; do
  if psql -h "$SOCKET_DIR" -p "$PORT" -U "$DB_USER" -d postgres -c "select 1" >/dev/null 2>&1; then
    break
  fi
  sleep 0.5
done

psql -h "$SOCKET_DIR" -p "$PORT" -U "$DB_USER" -d postgres -tc "select 1" >/dev/null
createdb -h "$SOCKET_DIR" -p "$PORT" -U "$DB_USER" "$DB_NAME"

cd "$ROOT_DIR"
RUN_POSTGRES_SMOKE=true \
DATABASE_URL="jdbc:postgresql://localhost:$PORT/$DB_NAME" \
DATABASE_USERNAME="$DB_USER" \
DATABASE_PASSWORD="" \
mvn test -Dtest=PostgresProfileSmokeTest
