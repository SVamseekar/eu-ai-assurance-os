# Security and Compliance Model

## Tenant Isolation

MVP / current ship:

- Tenant discriminator column on every tenant-owned table.
- API middleware injects tenant context from **verified credentials** only (`Authorization: Bearer` JWT or `X-Api-Key`). Client-supplied `X-Tenant-Id` / `X-Actor-Id` are **not** trusted.
- Queries always include tenant filters; cross-tenant ID access returns 404.
- CI regression: `TenantIsolationTest` (two tenants; never leak B data to A).

Production hardening options (not all required for current deploy):

- Row-level security in PostgreSQL.
- Enterprise option for schema-per-tenant or dedicated deployment.

See also: `docs/ROLE_MATRIX.md`, `docs/NFR.md`, Part 10.

## Authentication and Authorization

Roles (API enum `UserRole`):

- Admin (`ADMIN`)
- AI Engineering Lead (`AI_ENGINEERING_LEAD`)
- Compliance Officer (`COMPLIANCE_OFFICER`)
- Legal Counsel (`LEGAL_COUNSEL`)
- Auditor (`AUDITOR`)

(PRD/SECURITY “Data Platform Owner” maps to Admin or Engineering for contract mutations in the MVP matrix.)

Controls:

- **Password JWT + refresh tokens** and **API keys** for day-to-day API access.
- **OAuth/OIDC (Google + Microsoft)** — **implemented** (Part 4 / PR #27): authorization-code start + BFF callback exchange. **Production smoke pending** — do not claim production SSO verified until `docs/oauth-production-smoke-test.md` is executed and signed off.
- RBAC for system edits, approvals, exports, and audit access — full matrix in `docs/ROLE_MATRIX.md`.
- Service accounts for CI/CD and eval workers (`X-Api-Key`).
- Eval callback and manual execution endpoints require Admin or AI Engineering Lead actors.
- Eval operations visibility is limited to Admin, AI Engineering Lead, and Compliance Officer actors.
- External eval callbacks require timestamped HMAC-SHA-256 signatures over the raw request body (`EVAL_CALLBACK_SECRET`).

### Unauthenticated path allowlist

`TenantContextFilter` allows **only**:

| Path | Purpose |
|---|---|
| `/.well-known/jwks.json` | JWT public keys |
| `/auth/login`, `/auth/refresh`, `/auth/logout` | Token lifecycle |
| `/auth/oauth/**` | OAuth start + token exchange (BFF) |
| `/actuator/health` (+ `/liveness`, `/readiness` prefix) | Probes |

All other routes require a valid Bearer token or API key. Spring Security is intentionally `permitAll` so this filter remains the single authentication gate — do not bypass it.

### Dashboard session hard-gate

Next.js `middleware.ts` redirects unauthenticated browsers away from app shell routes (`/command`, `/systems`, `/approvals`, `/evidence`, `/evals`, `/contracts`, `/audit`, `/readiness`, `/reg-monitor`, …) to `/login`. API access goes through the BFF proxy with httpOnly cookies; the browser never sends tenant headers.

### Session cookies

Dashboard BFF sets httpOnly, `Secure` (production), `SameSite` cookies for access/refresh tokens after password or OAuth login. Prefer short-lived access JWTs and rotating refresh tokens (server-side hashed).

## Sensitive Data

- Do not send raw personal data to external LLM providers by default.
- Redact or tokenize PII before embedding.
- Encrypt evidence documents at rest (provider/object-store default encryption).
- Store API keys in a secret manager; only SHA-256 hashes in DB.
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

| Secret | Purpose |
|---|---|
| `EVAL_CALLBACK_SECRET` | HMAC for eval result callbacks |
| `AUDIT_CHAIN_SECRET` | HMAC material for audit hash-chain (`prev`/`event` hashes) |
| `DATABASE_PASSWORD` | DB credentials |
| OAuth client secrets | Google / Microsoft apps (env / secret manager) |
| `ASSURANCE_STORAGE_*` | Optional S3/MinIO credentials |

- **Never committed** — gitleaks (Part 1) and code review. Root `.env.example` is template only.
- Full ops detail: `docs/NFR.md`, `docs/DEPLOYMENT.md`.

## Audit Requirements

Audit events are **append-only** and **hash-chained** (HMAC-SHA-256 over event content + previous hash). Columns: `prev_event_hash`, `event_hash`, `retain_until` (default ≥ 7 years). Verify endpoints:

- `GET /api/v1/audit/verify`
- `GET /api/v1/audit-events/verify-chain`

High-value events include:

- Risk classification changed; high-risk system created.
- Evidence query answered; evidence pack exported (JSON/PDF) with `contentSha256`.
- Eval gate completed; data contract breach detected.
- Approval / override submitted.
- Determination run completed; certification readiness assessed/exported; reg item reviewed.

Production storage:

- **Shipped:** PostgreSQL/H2 append-only table with restricted write path + hash chain.
- Optional later: Kafka audit topic or WORM object storage for multi-region enterprise — **not required** for current architecture.

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
- OAuth state CSRF / token confusion if misconfigured.
- Overclaiming certification or legal determination via product UI copy.

Mitigations:

- Tenant-scoped retrieval and authorization from verified credentials only.
- Evidence chunk source validation; upload limits; URI schemes; checksums; metadata shape; chunk hashes; embedding-provider provenance.
- Citation-required answer generation.
- Reviewer approval workflows with oversight evidence.
- Hash-chained immutable audit events + verify API.
- Short-window callback signatures with no default production secret.
- Pessimistic eval run claims with Postgres `FOR UPDATE SKIP LOCKED`.
- Product disclaimers: assisted determination / readiness only — never notified-body language.
- OAuth state signed; BFF-mediated callback; production smoke runbook before claiming SSO live.

## Remaining residual risks (honest)

- OAuth **production** configuration and smoke not yet signed off.
- p95 latency and 99.9% uptime are **targets**, not measured production SLOs (see `docs/NFR.md`).
- Sector packs are SPI + stubs — no live proprietary connectors.
- Reg monitor remote sources disabled by default; curated bootstrap is assistive, not legal authority.
- PDF extraction from arbitrary binary object-store docs is still limited vs text upload path.

## EU Context

The product supports EU-focused assurance workflows. It does **not** make final legal determinations, issue certificates, or replace notified-body / qualified counsel review. It helps teams gather evidence, enforce controls, and prepare documentation for legal/compliance review.
