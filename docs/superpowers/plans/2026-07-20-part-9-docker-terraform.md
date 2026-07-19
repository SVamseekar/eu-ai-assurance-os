# Part 9 — Deployment: Docker Full Stack + Terraform Skeleton

> **For agentic workers:** Prefer working local compose first; Terraform is skeleton + docs, not necessarily applied cloud spend.

**Goal:** ROADMAP Phase 6 “Deployment with Docker and Terraform” + PRD §9 enterprise deploy story.

**Depends on:** Part 0; API postgres profile works.

**Existing:** `infra/docker-compose.yml`, `infra/Dockerfile`, partial README.

---

### Task 9.1: Docker Compose full local stack

- [ ] **Step 1:** Services:
  - `postgres` (pgvector) — already  
  - `api` (always on, not only profile `full` — or document `compose --profile full`)  
  - optional `dashboard` (Node build or `node:22` dev)  
  - optional `minio` for S3-compatible evidence storage when `FileStorage` enabled  
- [ ] **Step 2:** `.env.example` at repo root: `EVAL_CALLBACK_SECRET`, DB, `ASSURANCE_API_BASE_URL`, OAuth placeholders, `DISCORD_*`, GA id, `AUDIT_CHAIN_SECRET`.
- [ ] **Step 3:** Healthchecks for api + postgres; dashboard depends on api.
- [ ] **Step 4:** `infra/README.md` one-command: `docker compose up --build`.
- [ ] **Step 5:** Verify Flyway V1–latest on fresh volume.

### Task 9.2: Production Dockerfile polish

- [ ] **Step 1:** Multi-stage Maven build for API (already if present — verify non-root user, JRE image).
- [ ] **Step 2:** Dashboard: ensure Vercel remains primary; optional Dockerfile for self-host.
- [ ] **Step 3:** Document resource limits.

### Task 9.3: Terraform skeleton

- [ ] **Step 1:** `infra/terraform/` modules (skeleton):
  - `network` (VPC placeholders)
  - `database` (RDS Postgres or Cloud SQL module stub)
  - `secrets` (secret manager refs)
  - `compute` (Cloud Run / ECS service stub)
- [ ] **Step 2:** `variables.tf`, `outputs.tf`, `README.md` with “not applied by default; fill provider credentials.”
- [ ] **Step 3:** Example `terraform.tfvars.example`.
- [ ] **Step 4:** **Do not** require real cloud credentials in CI; `terraform fmt` + `validate` with mock backend if feasible.

### Task 9.4: Object store + embedding prod path (Phase 2 leftovers)

- [ ] **Step 1:** Document enabling S3/`FileStorageService` + `djl-sentence` in compose/postgres.
- [ ] **Step 2:** Smoke script: upload file → query → citation (optional, scripts/).
- [ ] **Step 3:** If gaps found (broken config), fix as part of this plan.

### Task 9.5: Deploy runbook

- [ ] **Step 1:** `docs/DEPLOYMENT.md`: Vercel dashboard + API host options (Cloud Run/VM), env matrix, migration order, rollback.
- [ ] **Step 2:** Link from root README (Part 11).

### Done when

- [ ] `docker compose` brings up postgres + api (+ dashboard optional) on clean machine instructions  
- [ ] Terraform skeleton validates  
- [ ] Deployment runbook exists  
- [ ] Object store / DJL path documented and smokeable  
