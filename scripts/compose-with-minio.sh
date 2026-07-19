#!/usr/bin/env bash
# Start postgres + api + minio with FileStorage pointed at MinIO (Part 9).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

ENV_FILE="${ENV_FILE:-.env.example}"
if [[ -f .env ]]; then
  ENV_FILE=.env
fi

export ASSURANCE_STORAGE_ENABLED=true
export ASSURANCE_STORAGE_ENDPOINT="${ASSURANCE_STORAGE_ENDPOINT:-http://minio:9000}"
export ASSURANCE_STORAGE_ACCESS_KEY_ID="${ASSURANCE_STORAGE_ACCESS_KEY_ID:-minioadmin}"
export ASSURANCE_STORAGE_SECRET_ACCESS_KEY="${ASSURANCE_STORAGE_SECRET_ACCESS_KEY:-minioadmin}"
export ASSURANCE_STORAGE_BUCKET="${ASSURANCE_STORAGE_BUCKET:-eu-ai-assurance-evidence}"
export ASSURANCE_STORAGE_PATH_STYLE="${ASSURANCE_STORAGE_PATH_STYLE:-true}"
export ASSURANCE_STORAGE_REGION="${ASSURANCE_STORAGE_REGION:-eu-west-1}"

echo "Starting stack with MinIO storage (env-file=$ENV_FILE)..."
exec docker compose -f infra/docker-compose.yml --env-file "$ENV_FILE" --profile minio up --build "$@"
