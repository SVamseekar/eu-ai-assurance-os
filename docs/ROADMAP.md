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

- Choose backend stack: Spring Boot 3 recommended for this project.
- Add PostgreSQL schema with Flyway.
- Implement tenant, user, and role model.
- Implement AI system registry APIs.
- Implement audit event append API.
- Add release gate calculation service.
- Add backend tests for release gate logic.

## Phase 2: Evidence and RAG

- Evidence document upload metadata.
- Text extraction pipeline.
- Chunking and embedding.
- pgvector similarity search.
- Citation-required answer generation.
- Prompt injection safeguards for evidence documents.

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
