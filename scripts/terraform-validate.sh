#!/usr/bin/env bash
# Format-check + validate Terraform skeleton without cloud credentials (Part 9).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TF_DIR="$ROOT/infra/terraform"

if command -v terraform >/dev/null 2>&1; then
  TF=(terraform)
else
  echo "Host terraform not found; using hashicorp/terraform:1.9 Docker image"
  TF=(docker run --rm -v "$ROOT":/work -w /work/infra/terraform hashicorp/terraform:1.9)
fi

cd "$TF_DIR"
"${TF[@]}" fmt -check -recursive
"${TF[@]}" init -backend=false -input=false
"${TF[@]}" validate
echo "Terraform skeleton OK"
