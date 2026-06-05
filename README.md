# EU AI Assurance OS

EU AI Assurance OS is a governance control plane for teams shipping AI systems in the European market. It validates AI releases against EU-focused controls by combining system inventory, risk classification, cited compliance evidence, evaluation gates, data-contract drift monitoring, approval workflows, and audit-ready evidence packs.

This repo starts with a verified static product prototype and a production-oriented project foundation. The next implementation phase should add a real backend, persistence, authentication, RAG retrieval, evaluation workers, and CI/CD release gates.

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
- Compliance evidence RAG simulation
- Eval gate simulation
- Data-contract drift simulation
- Append-only audit timeline
- Evidence pack JSON export
- Local state persistence
- Light/dark mode

## Repository Structure

```text
apps/web/       Static interactive prototype
docs/           PRD, architecture, API, schema, security, roadmap
services/api/   Placeholder for Spring Boot or FastAPI backend
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

Build the backend MVP:

1. Auth and tenant model
2. AI system registry APIs
3. Control library and risk classification APIs
4. Evidence document ingestion
5. Basic RAG retrieval with citations
6. Eval run records and release gate calculation
7. Data contract status APIs
8. Audit event append API
