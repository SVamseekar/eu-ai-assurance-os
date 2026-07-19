# Security and Compliance Model

## Tenant Isolation

MVP:

- Tenant discriminator column on every tenant-owned table.
- API middleware injects tenant context from **verified credentials** only (`Authorization: Bearer` JWT or `X-Api-Key`). Client-supplied `X-Tenant-Id` / `X-Actor-Id` are **not** trusted.
- Queries always include tenant filters; cross-tenant ID access returns 404.
- CI regression: `TenantIsolationTest` (two tenants; never leak B data to A).

Production:

- Row-level security in PostgreSQL.
- Enterprise option for schema-per-tenant or dedicated deployment.

See also: `docs/ROLE_MATRIX.md`, `docs/NFR.md`.

## Authentication and Authorization

Roles (API enum `UserRole`):

- Admin (`ADMIN`)
- AI Engineering Lead (`AI_ENGINEERING_LEAD`)
- Compliance Officer (`COMPLIANCE_OFFICER`)
- Legal Counsel (`LEGAL_COUNSEL`)
- Auditor (`AUDITOR`)

(PRD/SECURITY “Data Platform Owner” maps to Admin or Engineering for contract mutations in the MVP matrix.)

Controls:

- SSO/SAML or OIDC for production (Part 4).
- RBAC for system edits, approvals, exports, and audit access — full matrix in `docs/ROLE_MATRIX.md`.
- Service accounts for CI/CD and eval workers (`X-Api-Key`).
- Eval callback and manual execution endpoints require Admin or AI Engineering Lead actors.
- Eval operations visibility is limited to Admin, AI Engineering Lead, and Compliance Officer actors.
- External eval callbacks require timestamped HMAC signatures over the raw request body.

### Unauthenticated path allowlist

`TenantContextFilter` allows **only**:

| Path | Purpose |
|---|---|
| `/.well-known/jwks.json` | JWT public keys |
| `/auth/login`, `/auth/refresh`, `/auth/logout` | Token lifecycle |
| `/actuator/health` (+ `/liveness`, `/readiness` prefix) | Probes |

All other routes require a valid Bearer token or API key. Spring Security is intentionally `permitAll` so this filter remains the single authentication gate — do not bypass it.

### Dashboard session hard-gate

Next.js `middleware.ts` redirects unauthenticated browsers away from app shell routes (`/command`, `/systems`, `/approvals`, `/evidence`, `/evals`, `/contracts`, `/audit`) to `/login`. API access still goes through the BFF proxy with httpOnly cookies; the browser never sends tenant headers.

## Sensitive Data

- Do not send raw personal data to external LLM providers by default.
- Redact or tokenize PII before embedding.
- Encrypt evidence documents at rest.
- Store API keys in a secret manager.
- Track data region and retention settings per tenant.

## Encryption and secrets (NFR)

### In transit

- TLS terminates at the edge for the dashboard (Vercel HTTPS or reverse proxy).
- Production API and BFF→API traffic should use HTTPS (`ASSURANCE_API_BASE_URL`).
- Managed Postgres and object storage connections use provider TLS.

### At rest

- **Postgres disk encryption** is a **provider responsibility** (RDS/Cloud SQL/etc.); the app does not field-encrypt every column.
- Object storage should use bucket default encryption (SSE-S3/SSE-KMS or equivalent).
- API keys stored as SHA-256 hashes only; passwords BCrypt-hashed.

### Secrets

- `EVAL_CALLBACK_SECRET`, `AUDIT_CHAIN_SECRET`, `DATABASE_PASSWORD`, and future OAuth secrets come from env / a secret manager — **never committed**.
- Part 1 gitleaks / secret scanning is the repo control against accidental commits.
- Full ops detail: `docs/NFR.md`.

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
- Forged or replayed eval callbacks.
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
- Short-window callback signatures with no default production secret.
- Pessimistic eval run claims with Postgres `FOR UPDATE SKIP LOCKED` for multi-worker safety.
- PII detection and redaction.
- Release gate calculation that blocks on critical missing controls.

## EU Context

The product supports EU-focused assurance workflows. It does not make final legal determinations. It helps teams gather evidence, enforce controls, and prepare documentation for legal/compliance review.
