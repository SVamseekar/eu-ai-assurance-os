# EU AI Assurance OS

EU AI Assurance OS is a governance control plane for teams shipping AI systems in the European market. It validates AI releases against EU-focused controls by combining system inventory, risk classification, cited compliance evidence, evaluation gates, data-contract drift monitoring, approval workflows, reviewer notifications, human oversight evidence, and audit-ready evidence packs.

This repo starts with a verified static product prototype and a production-oriented Spring Boot backend foundation. The current MVP includes persistence, tenant-scoped APIs, audit trails, release gates, eval run records, data-contract drift simulation, approval workflows, and production-hardened evidence RAG foundations.

## Why This Project

The source reference files contained multiple strong PRD ideas:

- ComplianceGuard RAG: cited compliance answers, audit logs, EU AI Act readiness.
- EvalForge: continuous model/prompt evaluation, CI/CD regression gates.
- Data Contracts AI: schema drift, lineage, and data quality checks.
- Java/Spring Boot regulated-system ideas: auditability, SAGA workflows, multi-tenancy, security, and compliance-grade APIs.

EU AI Assurance OS merges those concepts into one coherent EU-context product.

## Current Prototype

Run the static app:

```bash
python3 -m http.server 4173 --directory apps/web
```

Then open:

```text
http://localhost:4173
```

Implemented:

- Command dashboard
- AI system registry
- EU risk topology visualization
- Compliance evidence RAG with API-backed document indexing and cited retrieval
- Eval gate simulation
- Data-contract drift simulation
- Append-only audit timeline
- Evidence pack JSON export
- Local state persistence
- Light/dark mode

## Repository Structure

```text
apps/web/       Static interactive prototype with optional API-backed evidence flow
docs/           PRD, architecture, API, schema, security, roadmap
services/api/   Spring Boot backend MVP
infra/          Placeholder for Docker, Terraform, and deployment manifests
scripts/        Local development and data scripts
```

## Production Target

Frontend:

- React or Next.js dashboard
- Role-based workflows for compliance, legal, AI engineering, and data platform teams

Backend:

- Spring Boot 3 or FastAPI
- PostgreSQL for core entities
- pgvector for cited evidence search
- Redis for rate limits and cache
- Kafka for eval jobs, data drift events, and audit append streams

AI layer:

- RAG over policies, DPIAs, model cards, vendor docs, data contracts, and control mappings
- Eval workers for faithfulness, relevance, safety refusal, bias slices, latency, and cost
- Human review workflows for high-risk overrides

Compliance:

- Multi-tenant isolation
- RBAC and SSO
- GDPR delete/export workflows
- Immutable audit ledger
- EU AI Act technical documentation evidence pack

## Next Milestone

Continue production hardening beyond Phase 2:

1. Implement a non-local embedding provider adapter behind the existing provider seam.
2. Wire object-store document extraction for PDFs and office documents.
3. Add authentication, RBAC, and request-scoped actor resolution.
4. Move eval and drift workflows onto async workers.
5. Add production monitoring for RAG faithfulness, latency, cost, refusal behavior, and citation quality.
