# Implementation Roadmap

## Phase 0: Prototype

Status: complete.

- Static dashboard.
- Simulated system registry.
- Simulated RAG answer.
- Simulated eval gate.
- Simulated data-contract drift.
- Evidence pack export.
- README and engineering docs.

## Phase 1: Backend Foundation

Status: complete.

- Choose backend stack: Spring Boot 3 recommended for this project.
- Add PostgreSQL schema with Flyway.
- Implement tenant, user, and role model.
- Implement AI system registry APIs.
- Implement audit event append API.
- Add release gate calculation service.
- Add backend tests for release gate logic.

## Phase 2: Evidence and RAG

Status: MVP complete.

- Evidence document upload metadata: implemented.
- Text extraction pipeline: implemented for provided text with metadata fallback.
- Chunking and embedding: implemented with deterministic local embeddings.
- pgvector similarity search: local cosine search implemented; production pgvector index remains.
- Citation-required answer generation: implemented.
- Prompt injection safeguards for evidence documents: implemented.
- Static web app upload/list/query flow: implemented with API fallback to demo mode.

Production hardening remains: external object-store extraction, provider-backed
embeddings, and PostgreSQL pgvector/HNSW indexing.

## Phase 3: Eval Gates

- Eval dataset registry.
- Eval run creation and status tracking.
- Async worker queue.
- Metrics capture: faithfulness, relevance, safety refusal, bias slices, latency, cost.
- Threshold-based release decision update.

## Phase 4: Data Contracts

- Data contract CRUD.
- Drift event ingestion.
- Contract-to-system mapping.
- Lineage display.
- Release gate integration.

## Phase 5: Workflow

- Approval stages.
- Reviewer assignment.
- Override workflow.
- Human oversight evidence capture.
- Notifications.

## Phase 6: Enterprise Readiness

- SSO/OIDC.
- Tenant isolation hardening.
- Immutable audit storage.
- Evidence pack PDF/JSON export.
- CI/CD release gate integration.
- Observability dashboards.
- Deployment with Docker and Terraform.
