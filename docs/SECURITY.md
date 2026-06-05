# Security and Compliance Model

## Tenant Isolation

MVP:

- Tenant discriminator column on every tenant-owned table.
- API middleware injects tenant context from token claims.
- Queries always include tenant filters.

Production:

- Row-level security in PostgreSQL.
- Enterprise option for schema-per-tenant or dedicated deployment.

## Authentication and Authorization

Roles:

- Admin
- AI Engineer
- Compliance Officer
- Legal Reviewer
- Data Platform Owner
- Auditor

Controls:

- SSO/SAML or OIDC for production.
- RBAC for system edits, approvals, exports, and audit access.
- Service accounts for CI/CD and eval workers.

## Sensitive Data

- Do not send raw personal data to external LLM providers by default.
- Redact or tokenize PII before embedding.
- Encrypt evidence documents at rest.
- Store API keys in a secret manager.
- Track data region and retention settings per tenant.

## Audit Requirements

Audit events must be append-only. High-value events:

- Risk classification changed.
- High-risk system created.
- Evidence query answered.
- Eval gate completed.
- Data contract breach detected.
- Approval submitted.
- Override submitted.
- Evidence pack exported.

Production storage options:

- PostgreSQL append-only table with restricted write path.
- Kafka audit topic.
- Object storage with write-once retention for enterprise.

## Threat Model

Primary risks:

- Cross-tenant data leakage.
- Prompt injection in evidence documents.
- Incomplete or fabricated citations.
- Unauthorized release approval.
- Missing audit trail for high-risk decision.
- PII leakage to model providers.
- Data-contract drift causing unvalidated model behavior.

Mitigations:

- Tenant-scoped retrieval.
- Evidence chunk source validation.
- Evidence upload limits, allowed URI schemes, checksum validation, metadata shape validation, chunk hashes, and embedding-provider provenance.
- Citation-required answer generation.
- Reviewer approval workflows.
- Immutable audit events.
- PII detection and redaction.
- Release gate calculation that blocks on critical missing controls.

## EU Context

The product supports EU-focused assurance workflows. It does not make final legal determinations. It helps teams gather evidence, enforce controls, and prepare documentation for legal/compliance review.
