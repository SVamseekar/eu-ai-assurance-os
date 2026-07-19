# Infrastructure (Part 9)

Local Docker stack and Terraform skeleton for EU AI Assurance OS.

## One-command local stack

From the **repository root**:

```bash
docker compose -f infra/docker-compose.yml --env-file .env.example up --build
```

This starts:

| Service | Port | Notes |
|---|---|---|
| `postgres` | 5432 | `pgvector/pgvector:pg16` |
| `api` | 8080 | Multi-stage image from `infra/Dockerfile`, profile `postgres` |

Health:

```bash
curl -fsS http://localhost:8080/actuator/health
```

Copy `.env.example` → `.env` and change `EVAL_CALLBACK_SECRET` / `AUDIT_CHAIN_SECRET` before any shared use.

### Optional profiles

```bash
# Self-host dashboard on :3000 (production UI is still Vercel)
docker compose -f infra/docker-compose.yml --env-file .env.example --profile dashboard up --build

# MinIO S3-compatible store on :9000 (console :9001)
docker compose -f infra/docker-compose.yml --env-file .env.example --profile minio up --build

# Both extras
docker compose -f infra/docker-compose.yml --env-file .env.example --profile full up --build
```

MinIO + storage-enabled API helper:

```bash
./scripts/compose-with-minio.sh
```

Evidence upload → query smoke (API must be healthy):

```bash
./scripts/compose-evidence-smoke.sh
```

### Images

| File | Purpose |
|---|---|
| `Dockerfile` | API multi-stage Maven → non-root JRE 17, healthcheck |
| `Dockerfile.dashboard` | Optional Next.js standalone self-host |

### Resource guidance

- **API + DJL:** allow **1–2 GB** RAM; first boot may download ONNX model weights.
- **Postgres:** ≥ 512 MB.
- JVM uses `-XX:MaxRAMPercentage=75.0` inside the API container.

Full runbook: [`docs/DEPLOYMENT.md`](../docs/DEPLOYMENT.md).

## Terraform skeleton

```bash
cd infra/terraform
terraform fmt -check -recursive
terraform init -backend=false
terraform validate
```

Or without a host install:

```bash
docker run --rm -v "$PWD":/work -w /work/infra/terraform hashicorp/terraform:1.9 \
  sh -c 'terraform fmt -check -recursive && terraform init -backend=false && terraform validate'
```

**Not applied by default** — no real cloud credentials in CI. See [`terraform/README.md`](./terraform/README.md).

## Layout

```text
infra/
  docker-compose.yml
  docker-compose.override.yml
  Dockerfile
  Dockerfile.dashboard
  terraform/          # network, database, secrets, compute stubs
  README.md
```
