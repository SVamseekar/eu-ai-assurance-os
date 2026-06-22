# Security Audit Remediation — Spec

**Source**: [docs/SECURITY_AUDIT_2026-06-22.md](../../SECURITY_AUDIT_2026-06-22.md)

**Decision record** (from user clarification on 2026-06-22):
- Auth model: email + password, server-issued JWT session (no external IdP).
- Signing: RS256 (asymmetric), API-hosted JWKS endpoint, key rotation via `kid`.
- Refresh tokens: stored hashed in DB, revocable, rotated on every use (theft detection via reuse of an already-rotated token).
- Frontend session: httpOnly, Secure, SameSite cookie set by the Next.js server; browser JS never sees the JWT. Next.js route handlers/middleware attach `Authorization: Bearer <jwt>` when proxying to the Spring Boot API.

This is a full authentication subsystem build, not a patch — there is currently no password storage, no login endpoint, no JWT library, and no Spring Security dependency in `services/api/`. Scope below treats it as a single project area but breaks into independently shippable pieces.

## In Scope

1. **Critical — Real authentication, replacing the unauthenticated legacy header fallback.**
2. **High — SSRF DNS-rebinding TOCTOU fix** in `TextExtractionService`.
3. **Medium — Prompt-injection guard hardening** (defense-in-depth improvement, not a silver bullet).
4. **Medium — `postcss`/`next` dependency bump** (dashboard).
5. **No action** — `hono` (confirmed unreachable transitive dev dependency, documented only).

## Out of Scope

- External IdP / OIDC federation (SSO) — `SECURITY.md` names this as a later production option; this spec builds the self-contained credential system it would federate with later, but does not implement SAML/OIDC client code.
- Full RBAC redesign — `UserRole` enum and `TenantAuthorizationService.requireAnyRole` already exist and are sound; this spec wires authentication into them, not redesigning authorization.
- Password reset / email verification flows — out of scope for this remediation; tracked as a fast-follow once login exists.
- Rate limiting / brute-force lockout on `/auth/login` — flagged as a fast-follow (see Task 9 note), not blocking this spec since it's additive hardening, not the vulnerability being fixed.

## 1. Authentication subsystem

### 1.1 Credential storage

Add a `password_hash` column to `users` (bcrypt, via `spring-boot-starter-security`'s `BCryptPasswordEncoder`, work factor 12). Existing seeded users (`BootstrapData`) get a seeded bcrypt hash for a known dev password, gated to non-`postgres`-profile seeding exactly like the existing API key seeding at `BootstrapData.java:81-90`.

### 1.2 Token model

- **Access token**: JWT, RS256, 15 minute expiry. Claims: `sub` (user id), `tenant_id`, `role`, `iat`, `exp`, `iss` (`eu-ai-assurance-os`), `kid` header matching the signing key.
- **Refresh token**: opaque random 256-bit value (not a JWT). Stored server-side as SHA-256 hash in a new `refresh_tokens` table, with `user_id`, `tenant_id`, `expires_at` (30 days), `created_at`, `revoked_at` (nullable), `replaced_by_token_hash` (nullable, for rotation-chain tracing). On every refresh: the presented token is hashed and looked up; if `revoked_at` is already set, every token in that rotation chain is revoked (reuse-after-rotation = compromise signal) and the request is rejected; otherwise a new refresh token is issued, the old one is marked revoked with `replaced_by_token_hash` pointing at the new hash.

### 1.3 Key management

RSA-2048 keypair. For this spec: generated at startup if absent, persisted to a new `signing_keys` table (`kid`, `algorithm`, `public_key_pem`, `private_key_pem`, `created_at`, `active` boolean) so restarts don't invalidate outstanding tokens. Exactly one key has `active = true` at a time; `JwksController` serves all non-expired public keys (active + recently-rotated) so tokens signed just before a rotation still verify.

Private key storage in the DB is acceptable for this MVP-stage product (same trust boundary as `DATABASE_PASSWORD` already in the DB connection) — note as a fast-follow to move to a secret manager / HSM at production scale, consistent with `SECURITY.md`'s "Store API keys in a secret manager" guidance for the next maturity stage.

### 1.4 Endpoints (new `auth` package, `os.assurance.eu.api.auth`)

- `POST /auth/login` — body `{email, password}`. Verifies bcrypt hash, issues access + refresh token pair. Returns `{accessToken, refreshToken, expiresIn}`. On failure: generic 401, no user-existence leakage (same error for "no such user" and "wrong password").
- `POST /auth/refresh` — body `{refreshToken}`. Validates and rotates per 1.2. Returns new `{accessToken, refreshToken, expiresIn}`.
- `POST /auth/logout` — body `{refreshToken}`. Revokes the token (and its chain). Idempotent.
- `GET /.well-known/jwks.json` — public, no auth required. Returns JWKS-formatted public keys.

### 1.5 Request authentication (replacing the legacy fallback)

`TenantContextFilter` is rewritten:
- `Authorization: Bearer <jwt>` → verify signature against active JWKS keys, check `exp`/`iss`, set `tenantContext` overrides from `tenant_id`/`sub` claims. The `role` claim is carried for observability/audit logging only — `TenantAuthorizationService.requireAnyRole` continues to re-query `UserJpaRepository` by `(actorId, tenantId)` on every authorization check (existing behavior, unchanged) rather than trusting the JWT's role claim, so a role change or user deactivation takes effect immediately rather than waiting for the access token to expire.
- `X-Api-Key` → unchanged, existing secure path for service accounts (CI/CD, eval workers per `SECURITY.md`).
- Neither present, or invalid/expired → `401`, **no fallback**. The `X-Tenant-Id`/`X-Actor-Id` legacy header path and `TenantContext.DEFAULT_TENANT_ID`/`DEFAULT_USER_ID` magic-UUID fallback are deleted entirely — not flagged, not profile-gated, removed. There is no production-safe reason to keep a header an unauthenticated client can set.

`TenantContext.headerUuid` fallback-to-header behavior is deleted along with it; `tenantId()`/`actorId()` only ever return the override set by the filter for an authenticated request.

### 1.6 Frontend session (`apps/dashboard/`)

- New Next.js Route Handler `app/api/auth/login/route.ts`: accepts `{email, password}` from a client form, calls Spring Boot `/auth/login` server-side, sets the returned access + refresh tokens as **two separate httpOnly, Secure, SameSite=Lax cookies** (`session_access`, `session_refresh`) on the Next.js response. Browser JS never reads or sets either.
- New Route Handler `app/api/auth/logout/route.ts`: reads `session_refresh` cookie, calls Spring Boot `/auth/logout`, clears both cookies.
- `lib/api.ts` is rewritten: `request()` no longer reads `localStorage` or sets `X-Tenant-Id`/`X-Actor-Id`. Instead, all `api.*` calls go through a new server-side proxy route `app/api/proxy/[...path]/route.ts` that: reads `session_access` cookie, attaches `Authorization: Bearer <token>`, forwards to the Spring Boot API; on a `401` response, transparently calls `/auth/refresh` using `session_refresh`, retries once, and re-sets rotated cookies on success — falling through to a `401` to the client (triggering a login redirect) if refresh also fails.
- A minimal login page (`app/login/page.tsx`) — email/password form posting to the new route handler — is required since there is currently no login UI at all.
- `apiHeaders` export (`lib/api.ts:16-19`, the hardcoded `tenant-premium`/`actor-priya` demo identity) is deleted.

## 2. SSRF DNS-rebinding fix (`TextExtractionService.java`)

JDK 17's `java.net.http.HttpClient` has no DNS-resolver override hook (`HttpClient.Builder.resolver(...)` doesn't exist on this JDK baseline), so the fix pins the connection to a validated IP directly rather than trying to intercept `HttpClient`'s internal resolution:

1. `validateNoSsrf` (existing, lines 92-104) resolves the host via `InetAddress.getAllByName`, validates every returned address, and now **returns the first validated `InetAddress`** instead of discarding it.
2. The request URI's host is rewritten to that literal IP address (e.g. `https://203.0.113.7/path`) before building the `HttpRequest`.
3. The original hostname is preserved via an explicit `Host` header on the request (required for any virtual-hosted target to route correctly) and via `HttpClient.Builder.sslParameters(SSLParameters)` with `setServerNames(List.of(new SNIHostName(originalHost)))` set on the client used for this request, so TLS SNI and certificate hostname verification still validate against the real hostname, not the IP literal.

This collapses the TOCTOU window to zero — the same validated address that was checked is the one connected to, because no second DNS lookup ever happens. Build a dedicated short-lived `HttpClient` per extraction call (or a small per-request builder) since `sslParameters` is set at client-construction time, not per-request.

Add a unit test that fakes a hostname resolving to a public IP on first lookup and asserts the connection uses that exact IP with no second resolution — verifiable by injecting a test seam (e.g. extracting the resolve-then-build logic into a package-private method that takes an `InetAddress` resolver function) so the test can assert call count of 1.

## 3. Prompt-injection guard hardening (`PromptInjectionGuard.java`)

Keep substring matching as a fast first pass but add: Unicode normalization (NFKC) before matching to defeat homoglyph/full-width-character tricks, zero-width character stripping (`​`-`‍`, `﻿`) before matching, and detection of phrases split across adjacent lines by also matching against the whole-document text with newlines collapsed to spaces (in addition to the existing per-line check, so a phrase split `"ignore\nprevious"` is still caught by the whole-text pass even though the per-line pass alone would miss it). This remains explicitly documented as defense-in-depth, not a complete mitigation — the citation-required answer generation already in place is the real backstop per `SECURITY.md`.

## 4. Dependency bump (`apps/dashboard/`)

Bump `next` to the latest patch/minor within the current major that resolves the `postcss` advisory (verify via `npm audit` after bump — do not blindly run `npm audit fix --force`, which the audit doc already flagged as suggesting the wrong direction).

## Testing requirements

- Unit tests for: bcrypt verification, JWT issuance/verification round-trip, refresh token rotation (including reuse-after-rotation revocation-of-chain behavior), JWKS endpoint shape, `TenantContextFilter` rejecting requests with no/invalid/expired credentials and accepting valid Bearer/API-key requests.
- Integration test: full login → authenticated request → refresh → logout → subsequent request with revoked refresh token fails.
- Regression test: confirm `X-Tenant-Id`/`X-Actor-Id` headers alone (no Bearer/API-key) no longer grant access (this is the exploit this spec closes — must have an explicit test proving it's closed).
- SSRF test: existing private/loopback/link-local rejection tests must still pass; add a test for the post-connection re-validation behavior using a mock/fake resolver if feasible, or document as manual-verification-only if not feasible in unit scope.
- Prompt-injection guard: add test cases for split-line and zero-width-character bypass attempts that the old implementation would have missed.
