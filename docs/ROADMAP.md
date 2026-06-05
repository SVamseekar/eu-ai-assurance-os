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

Status: production-hardened foundation complete.

- Evidence document upload metadata: implemented.
- Text extraction pipeline: implemented for provided text with metadata fallback.
- Chunking and embedding: implemented with deterministic local embeddings.
- Embedding provider seam: implemented with local deterministic provider and profile-based provider selection.
- pgvector similarity search: local cosine search implemented; PostgreSQL profile includes pgvector/HNSW migration path.
- Citation-required answer generation: implemented.
- Prompt injection safeguards for evidence documents: implemented.
- Upload/query guardrails: implemented for source URI scheme, checksum, content length, metadata shape, query length, chunk size, provider provenance, and content hashing.
- Static web app upload/list/query flow: implemented with API fallback to demo mode.

Deployment integrations remain: object-store/PDF extraction, non-local embedding
provider implementation, production auth/RBAC, and operational monitoring.

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
