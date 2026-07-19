# Deployment runbook (Part 9)

How to run EU AI Assurance OS locally with Docker and how to deploy the production split: **Vercel dashboard** + **hosted Spring API** + **Postgres**.

Related: [NFR.md](./NFR.md) (secrets/encryption), [OPS.md](./OPS.md) (CI gate + metrics), [SECURITY.md](./SECURITY.md), root [`.env.example`](../.env.example), [`infra/README.md`](../infra/README.md).

---

## 1. Local full stack (Docker Compose)

### Prerequisites

- Docker Engine + Compose v2
- Optional: copy root `.env.example` → `.env` and change secrets

### One command (postgres + API)

From the **repository root**:

```bash
docker compose -f infra/docker-compose.yml --env-file .env.example up --build
```

Or from `infra/`:

```bash
cp ../.env.example ../.env   # once
docker compose --env-file ../.env up --build
```

| Service | URL |
|---|---|
| API | http://localhost:8080 |
| Health | http://localhost:8080/actuator/health |
| Postgres | `localhost:5432` (user/db/password from env) |

Default stack always starts **postgres + api** (API is no longer behind a Compose profile).

### Optional profiles

```bash
# Self-hosted Next.js dashboard (Vercel remains primary for production)
docker compose -f infra/docker-compose.yml --env-file .env.example --profile dashboard up --build

# MinIO for S3-compatible evidence storage
docker compose -f infra/docker-compose.yml --env-file .env.example --profile minio up --build

# Dashboard + MinIO together
docker compose -f infra/docker-compose.yml --env-file .env.example --profile full up --build
```

With MinIO, set storage env (see `.env.example`):

```bash
ASSURANCE_STORAGE_ENABLED=true
ASSURANCE_STORAGE_ENDPOINT=http://minio:9000
ASSURANCE_STORAGE_ACCESS_KEY_ID=minioadmin
ASSURANCE_STORAGE_SECRET_ACCESS_KEY=minioadmin
ASSURANCE_STORAGE_BUCKET=eu-ai-assurance-evidence
ASSURANCE_STORAGE_PATH_STYLE=true
```

Helper: `scripts/compose-with-minio.sh`.

### Flyway on a fresh volume

On first API boot against empty Postgres, Flyway applies `V1`–latest under `services/api/src/main/resources/db/migration` plus postgres-only scripts (pgvector index is best-effort if the extension is missing).

Verify:

```bash
curl -fsS http://localhost:8080/actuator/health
# Login with bootstrap user (local only):
#   email: compliance@example.com
#   password: dev-local-password-only
```

### Resource notes

| Component | Suggested memory |
|---|---|
| Postgres | ≥ 512 MB |
| API (local-hash embeddings) | ≥ 512 MB |
| API (`djl-sentence` / ONNX) | **1–2 GB** (first start downloads model weights) |
| MinIO | ≥ 256 MB |
| Dashboard container | ≥ 512 MB |

The API image sets `-XX:MaxRAMPercentage=75.0` so the JVM leaves headroom for native DJL allocations.

### Evidence / DJL / object store path

| Mode | Config | Behavior |
|---|---|---|
| Dev H2 | `local-hash` | Portable text embeddings; no DJL |
| Compose default | `EVIDENCE_EMBEDDING_PROVIDER=local-hash` (in `.env.example`) | Fast local stack; no HF download |
| Postgres profile (app default when env unset) | `djl-sentence` | DJL + ONNX all-MiniLM-L6-v2; downloads model + `tokenizer.json` on first boot |
| Object store off | `ASSURANCE_STORAGE_ENABLED=false` | Upload API returns synthetic `s3://` URIs; content still ingested from request body when provided |
| Object store on | MinIO or real S3 | `FileStorageService` PUT/GET with path-style when endpoint set |

To exercise DJL in Compose, set `EVIDENCE_EMBEDDING_PROVIDER=djl-sentence` and allow 1–2 GB RAM plus outbound HTTPS to Hugging Face.

Smoke (API up, password login):

```bash
./scripts/compose-evidence-smoke.sh
```

---

## 2. Production topology (recommended)

```text
[ Browser ]
    │ HTTPS
    ▼
[ Vercel — apps/dashboard ]  ←── primary UI / landing / BFF proxy
    │ server-side ASSURANCE_API_BASE_URL
    ▼
[ API host — Spring Boot container ]  ←── Cloud Run / ECS / VM
    │
    ├── Postgres (managed, encryption at rest)
    └── Optional S3/GCS bucket (evidence binaries)
```

Terraform under `infra/terraform/` is a **skeleton** (validate/fmt only). Fill provider modules before any cloud apply — see `infra/terraform/README.md`.

---

## 3. Environment matrix

| Variable | API | Dashboard (Vercel) | Notes |
|---|---|---|---|
| `EVAL_CALLBACK_SECRET` | **Required** | — | Fail closed if empty in shared envs |
| `AUDIT_CHAIN_SECRET` | **Required** (prod) | — | Hash-chain HMAC; rotate with care |
| `DATABASE_URL` / `USERNAME` / `PASSWORD` | **Required** | — | Postgres profile |
| `SPRING_PROFILES_ACTIVE` | `postgres` | — | |
| `EVIDENCE_EMBEDDING_PROVIDER` | `djl-sentence` recommended | — | Or `local-hash` for cheap envs |
| `ASSURANCE_STORAGE_*` | Optional | — | S3/MinIO; see §1 |
| `ASSURANCE_API_BASE_URL` | — | **Required** | Server-side only (BFF) |
| `NEXT_PUBLIC_SITE_URL` | — | Recommended | Canonical SEO URL |
| `NEXT_PUBLIC_GA_MEASUREMENT_ID` | — | Optional | GA4 |
| OAuth client ids/secrets | Future Part 4 | Future Part 4 | Placeholders in `.env.example` |
| `DISCORD_*` | Optional ops | Optional | Never commit tokens |

Full template: [`.env.example`](../.env.example). Secrets handling: [NFR.md](./NFR.md#encryption-and-secrets-nfr).

---

## 4. Deploy order

### A. Database

1. Provision Postgres 16+ (prefer same region as API).
2. Create role/database; store password in a secret manager.
3. Optionally enable `CREATE EXTENSION vector;` for HNSW (migration skips gracefully if absent).

### B. API

1. Build image: `docker build -f infra/Dockerfile -t eu-ai-assurance-api:$TAG .`
2. Push to your registry.
3. Set env from secret manager (table above).
4. Expose port 8080; probes:
   - Liveness: `GET /actuator/health/liveness`
   - Readiness: `GET /actuator/health/readiness`
5. **Migrations:** Flyway runs automatically on application startup. Do not run ad-hoc SQL that races the API.
6. Confirm `GET /actuator/health` is UP before pointing the dashboard at the API.

### C. Dashboard (Vercel)

1. Import `apps/dashboard` (or monorepo root with dashboard as project root).
2. Set:
   - `ASSURANCE_API_BASE_URL=https://api.example.com`
   - `NEXT_PUBLIC_SITE_URL=https://euassuranceai.example.com`
   - Optional `NEXT_PUBLIC_GA_MEASUREMENT_ID`
3. Deploy production; verify login + `/api/proxy` against the API.

### D. Post-deploy checks

```bash
curl -fsS https://api.example.com/actuator/health
# Optional CI gate (docs/OPS.md):
ASSURANCE_API_BASE=https://api.example.com \
API_KEY=... SYSTEM_ID=... ./scripts/ci-release-gate.sh
```

---

## 5. Migration order & rollback

### Forward

1. Ship API image that includes the new Flyway versions.
2. Deploy API → Flyway applies pending `V*` scripts in one transaction per version.
3. Deploy dashboard only if UI depends on new API fields.

### Rollback

| Layer | Strategy |
|---|---|
| Dashboard (Vercel) | Instant rollback to previous deployment in Vercel UI / CLI |
| API container | Redeploy previous image tag |
| Database | **Prefer expand/contract.** Flyway undo is not wired. Destructive down-migrations are not provided — restore from snapshot/PITR if a migration corrupts data |
| Secrets | Rotate in secret manager; rolling restart API so all tasks see the new value |

Never force-push Flyway `schema_history` repairs in production without a backup.

---

## 6. Self-host dashboard (optional)

```bash
docker build -f infra/Dockerfile.dashboard \
  --build-arg NEXT_PUBLIC_SITE_URL=https://app.example.com \
  -t eu-ai-assurance-dashboard .

docker run -p 3000:3000 \
  -e ASSURANCE_API_BASE_URL=https://api.example.com \
  eu-ai-assurance-dashboard
```

Requires `DOCKER_BUILD=1` standalone output (set in the Dockerfile). Prefer Vercel for TLS, previews, and edge caching.

---

## 7. Terraform

```bash
cd infra/terraform
terraform fmt -check -recursive
terraform init -backend=false
terraform validate
```

No cloud credentials required for validation. Do not `apply` the skeleton as-is.

---

## 8. Security checklist (deploy)

- [ ] TLS on all public endpoints
- [ ] Secrets only from env / secret manager (gitleaks clean)
- [ ] Metrics/prometheus not public without authz network policy ([OPS.md](./OPS.md))
- [ ] Eval callback secret non-empty
- [ ] Audit chain secret set and backed up for verification
- [ ] Bootstrap passwords (`dev-local-password-only`) **disabled or rotated** outside local Compose
