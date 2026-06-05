# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Project Is

EU AI Assurance OS is a governance control plane for teams shipping AI systems in the EU market. It validates AI releases against EU AI Act controls by combining: AI system registry, risk classification, compliance evidence RAG with citations, eval gate simulation, data-contract drift monitoring, approval workflows, and audit-ready evidence packs.

## Repository Layout

```
apps/dashboard/ Next.js 14 + TypeScript production dashboard (primary frontend)
apps/web/       Static HTML/CSS/JS prototype (legacy, kept for reference)
services/api/   Spring Boot 3 backend MVP (Java 17, Maven)
docs/           PRD, architecture, API contract, schema, roadmap, security
infra/          Placeholder (Docker, Terraform — not yet implemented)
scripts/        Local dev and smoke-test shell scripts
```

## Dashboard (`apps/dashboard/`)

### Build and Run

```bash
# Install dependencies
cd apps/dashboard && npm install

# Development server (proxies /api/v1/* to localhost:8080)
cd apps/dashboard && npm run dev
# → http://localhost:3000

# Production build
cd apps/dashboard && npm run build

# TypeScript check
cd apps/dashboard && npx tsc --noEmit
```

The dashboard falls back to seeded mock data when the Spring Boot API is unreachable. Start the API for full functionality. The proxy is configured in `next.config.ts` — requests to `/api/v1/*` are forwarded to `http://localhost:8080/api/v1/*`.

### Dashboard Architecture

- **Framework**: Next.js 16 (App Router), TypeScript, Tailwind CSS v4, shadcn/ui (Base UI)
- **Data fetching**: TanStack Query v5 with `placeholderData` for mock fallback
- **Lineage graph**: `@xyflow/react` — interactive DAG at `/contracts` showing data source → contract → AI system
- **Routing**: `app/(dashboard)/` route group with shared sidebar + header layout
- **API client**: `lib/api.ts` — typed fetch wrappers for all Spring Boot endpoints
- **Mock data**: `lib/mock-data.ts` — seeded demo data for offline/demo mode

### Running the Legacy Prototype

```bash
python3 -m http.server 4173 --directory apps/web
# → http://localhost:4173
```

## API Service (`services/api/`)

### Build and Run

```bash
# Run tests (uses H2 in-memory, no external deps needed)
cd services/api && mvn test

# Start the API (H2 profile, default)
cd services/api && mvn spring-boot:run
# → http://localhost:8080

# Run with PostgreSQL
DATABASE_URL=jdbc:postgresql://localhost:5432/eu_ai_assurance \
DATABASE_USERNAME=eu_ai_assurance \
DATABASE_PASSWORD=eu_ai_assurance \
EVAL_CALLBACK_SECRET=replace-with-secret-manager-value \
mvn spring-boot:run -Dspring-boot.run.profiles=postgres

# Run a single test class
mvn test -Dtest=ReleaseGateServiceTest
```

### Required Environment Variable

`EVAL_CALLBACK_SECRET` must be set in every runnable environment. Without it the eval result callback endpoint will reject all incoming callbacks. The default profile accepts an empty string for local H2 development.

### PostgreSQL Smoke Tests

```bash
# Smoke test (sets up a temp local DB)
bash services/api/scripts/postgres-smoke.sh

# Concurrency validation against an existing Postgres DB
RUN_POSTGRES_CONCURRENCY=true \
DATABASE_URL=jdbc:postgresql://localhost:5432/eu_ai_assurance \
DATABASE_USERNAME=eu_ai_assurance \
DATABASE_PASSWORD=eu_ai_assurance \
EVAL_CALLBACK_SECRET=test-eval-callback-secret \
mvn test -Dtest=PostgresEvalRunConcurrencyTest
```

## Backend Architecture

All code lives under `os.assurance.eu.api` in these domain packages:

| Package | Responsibility |
|---|---|
| `system` | AI system CRUD, risk classification, release gate, evidence pack |
| `evidence` | Document ingestion, chunking, embedding, cited RAG query |
| `eval` | Eval datasets, eval runs, background worker queue, callback |
| `contract` | Data contracts, drift events, status rollup |
| `audit` | Append-only audit event stream |
| `tenant` | `TenantContext`, `TenantContextFilter`, `TenantAuthorizationService` |
| `persistence` | Shared JPA converters (`JsonMapConverter`, `StringListConverter`) |

### Layer Conventions

Each domain follows: `Controller → Service → Repository (interface) → JpaRepository (impl) → Entity`. The `*Repository` interface is the domain seam; `*JpaRepository` extends Spring Data JPA. Domain model records (`AiSystem`, `EvalRun`, etc.) are separate from `*Entity` JPA classes and are mapped explicitly.

### Tenant Isolation

Every request is filtered through `TenantContextFilter`, which resolves `X-Tenant-Id` and `X-Actor-Id` headers against known DB records and falls back to the bootstrapped MVP tenant/actor when headers are absent. All JPA repositories scope queries by `tenantId`. The `BootstrapData` class seeds one tenant and user on startup for local development.

### Release Gate Logic

`ReleaseGateService` computes a `ReleaseDecision` (PASS / REVIEW / BLOCKED) from:
1. Evidence coverage — at least one indexed document per system
2. Eval score — latest completed eval run must meet the configured threshold
3. Data contract status — any open `BREACH` drift event propagates to BLOCKED
4. High-risk systems require human oversight SOP evidence

### Evidence RAG

`EvidenceEmbeddingProvider` is the pluggable seam. The current implementation (`LocalHashEvidenceEmbeddingProvider`) uses portable text hashes for local/H2 development. The `postgres` profile adds a pgvector HNSW index. The ingestion pipeline: `EvidenceIngestionGuard` (validation + prompt-injection stripping) → `EvidenceChunker` (splits content) → `EvidenceEmbeddingService` (embeds and stores chunks).

### Eval Worker

`EvalRunQueueWorker` polls the DB every 5 seconds (configurable via `assurance.eval.worker.poll-interval-ms`) for queued runs. It uses a select-for-update-skip-locked claim pattern (`EvalRunClaim`) to handle concurrent workers safely. `EvalRunWorkerService` owns the execute/retry/failure logic. The result callback endpoint (`PATCH /eval-runs/{id}/result`) requires `X-Eval-Timestamp` and `X-Eval-Signature: v1=<hex hmac sha256>` headers.

### Database Migrations

Flyway migrations in `src/main/resources/db/migration/`:
- `V1` — Phase 1: tenants, users, ai_systems, controls, system_controls, audit_events
- `V2` — Phase 2: evidence_documents, evidence_chunks, evidence_queries
- `V3` — Phase 2 production hardening: chunk SHA-256, embedding provider, metadata
- `V5` — Phase 3: eval_datasets, eval_runs (worker durability columns)
- `V6` — Phase 4: data_contracts, drift_events

`src/main/resources/db/postgresql/V4` adds pgvector HNSW index (postgres profile only; skipped gracefully if `vector` extension is not installed).

## Key Configuration Properties

```properties
# Embedding provider (default: local-hash)
assurance.evidence.embedding-provider=local-hash

# Eval worker
assurance.eval.worker.enabled=true
assurance.eval.worker.poll-interval-ms=5000
assurance.eval.callback.secret=${EVAL_CALLBACK_SECRET:}
assurance.eval.callback.signature-tolerance-seconds=300
```

## Operational Endpoints

- `GET /actuator/health` — liveness/readiness probes
- `GET /actuator/metrics` — includes `assurance.eval.run.*` counters

## Static Prototype (`apps/web/`)

Three files: `index.html`, `styles.css`, `app.js`. No build step. The prototype stores state in `localStorage` and optionally connects to the API for evidence upload/query (CORS is pre-configured for `localhost:4173`, `localhost:8000`, and Vite defaults). The evidence RAG flow in `app.js` is the integration point to test API connectivity.

## Docs

- `docs/PRD.md` — product requirements and user personas
- `docs/ARCHITECTURE.md` — system flowchart, service descriptions, release gate and audit strategy
- `docs/API.md` — full API contract with request/response examples
- `docs/SCHEMA.md` — all SQL table definitions
- `docs/ROADMAP.md` — phased delivery plan
- `docs/SECURITY.md` — threat model and security controls
