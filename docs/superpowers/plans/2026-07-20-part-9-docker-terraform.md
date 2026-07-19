# Part 9 — Deployment: Docker Full Stack + Terraform Skeleton

> **For agentic workers:** Prefer working local compose first; Terraform is skeleton + docs, not necessarily applied cloud spend.

**Goal:** ROADMAP Phase 6 “Deployment with Docker and Terraform” + PRD §9 enterprise deploy story.

**Depends on:** Part 0; API postgres profile works.

**Existing:** `infra/docker-compose.yml`, `infra/Dockerfile`, partial README.

---

### Task 9.1: Docker Compose full local stack

- [x] **Step 1:** Services:
  - `postgres` (pgvector) — already  
  - `api` (always on; `dashboard` / `minio` / `full` profiles)  
  - optional `dashboard` (`infra/Dockerfile.dashboard`)  
  - optional `minio` for S3-compatible evidence storage when `FileStorage` enabled  
- [x] **Step 2:** `.env.example` at repo root: `EVAL_CALLBACK_SECRET`, DB, `ASSURANCE_API_BASE_URL`, OAuth placeholders, `DISCORD_*`, GA id, `AUDIT_CHAIN_SECRET`.
- [x] **Step 3:** Healthchecks for api + postgres; dashboard depends on api.
- [x] **Step 4:** `infra/README.md` one-command: `docker compose up --build`.
- [x] **Step 5:** Verify Flyway V1–latest on fresh volume.

### Task 9.2: Production Dockerfile polish

- [x] **Step 1:** Multi-stage Maven build for API (non-root user, JRE image, healthcheck).
- [x] **Step 2:** Dashboard: Vercel remains primary; optional `infra/Dockerfile.dashboard` for self-host.
- [x] **Step 3:** Document resource limits.

### Task 9.3: Terraform skeleton

- [x] **Step 1:** `infra/terraform/` modules (skeleton):
  - `network` (VPC placeholders)
  - `database` (RDS Postgres or Cloud SQL module stub)
  - `secrets` (secret manager refs)
  - `compute` (Cloud Run / ECS service stub)
- [x] **Step 2:** `variables.tf`, `outputs.tf`, `README.md` with “not applied by default; fill provider credentials.”
- [x] **Step 3:** Example `terraform.tfvars.example`.
- [x] **Step 4:** **Do not** require real cloud credentials in CI; `terraform fmt` + `validate` with null provider / local backend.

### Task 9.4: Object store + embedding prod path (Phase 2 leftovers)

- [x] **Step 1:** Document enabling S3/`FileStorageService` + `djl-sentence` in compose/postgres.
- [x] **Step 2:** Smoke script: `scripts/compose-evidence-smoke.sh` (ingest + query + citations).
- [x] **Step 3:** Path-style S3 for MinIO endpoints; storage env binding.

### Task 9.5: Deploy runbook

- [x] **Step 1:** `docs/DEPLOYMENT.md`: Vercel dashboard + API host options (Cloud Run/VM), env matrix, migration order, rollback.
- [x] **Step 2:** Link from root README (full docs alignment remains Part 11).

### Done when

- [x] `docker compose` brings up postgres + api (+ dashboard optional) on clean machine instructions  
- [x] Terraform skeleton validates  
- [x] Deployment runbook exists  
- [x] Object store / DJL path documented and smokeable  
