# Security Audit — 2026-06-22

Full-codebase security audit of `services/api/` (Spring Boot backend) and `apps/dashboard/` + `apps/web/` (frontends), assessed against the threat model in [SECURITY.md](SECURITY.md).

## Critical

### 1. Tenant impersonation via unauthenticated headers

End-to-end exploitable from the browser, not just a backend issue.

**Backend** — `services/api/src/main/java/os/assurance/eu/api/tenant/TenantContextFilter.java:67-78` and `TenantContext.java:38-44`: when no `X-Api-Key` is sent, the filter falls back to trusting raw `X-Tenant-Id` / `X-Actor-Id` headers. It only verifies the IDs *exist* in the DB — not that the caller is entitled to claim them. Any unauthenticated request with `X-Tenant-Id: <victim-tenant-uuid>` and `X-Actor-Id: <victim-admin-uuid>` gets full tenant impersonation, including whatever role the impersonated actor holds — `TenantAuthorizationService.requireAnyRole` checks pass because the actor's role is trusted as given.

**Frontend** — `apps/dashboard/lib/api.ts:16-30`: the dashboard reads these same headers from `localStorage` (`eu-ai-tenant-id`, `eu-ai-actor-id`) with hardcoded demo defaults (`tenant-premium` / `actor-priya`). Any browser user can open devtools and run `localStorage.setItem("eu-ai-tenant-id", "<other-tenant>")` to read/write another tenant's data through the normal UI.

This is the exact "cross-tenant data leakage" risk `SECURITY.md` names as the primary threat. Nothing currently stops it in any reachable profile.

**Fix direction**: require a real authenticated session (JWT/OIDC) server-side; derive tenant/actor from verified token claims, never from client-supplied headers. Restrict the header-fallback path to a `dev`/`test` Spring profile only, gated so it cannot activate in any deployed environment. Frontend must stop self-asserting identity and instead carry a session token issued by the server.

## High

### 2. SSRF via evidence `sourceUri` — DNS-rebinding TOCTOU gap

**Correction to initial audit finding**: `TextExtractionService.java:87-112` (`validateNoSsrf`) already resolves the `sourceUri` host via `InetAddress.getAllByName` and rejects loopback, link-local (covers `169.254.0.0/16`, i.e. cloud metadata endpoints), site-local, any-local, and multicast addresses *before* fetching — so the "no protection at all" framing in the initial pass was inaccurate.

The real residual gap is **time-of-check/time-of-use (TOCTOU)**: `validateNoSsrf` (line 92) resolves the hostname once to validate it, then `HttpClient.send` (line 51) independently resolves the same hostname again when it actually connects. A malicious or attacker-controlled DNS server can return a public IP for the first lookup (passing validation) and a private/link-local IP for the second lookup moments later (the actual connection) — classic DNS rebinding. `HttpClient.Redirect.NEVER` (line 26) prevents the redirect-based variant of this bypass but not the rebinding variant, since no redirect is involved.

**Fix implemented (2026-06-22)**: `java.net.http.HttpClient` on JDK 17 has no DNS-resolver override hook, and connecting to a pinned IP literal while setting a `Host` header is not viable either — the JDK rejects `Host` as a restricted header (`IllegalArgumentException: restricted header name: "Host"`), so an earlier attempt at this fix silently degraded every evidence fetch to a metadata-only stub instead of throwing. The actual fix replaces the JDK `HttpClient` with Apache HttpClient5 (`org.apache.httpcomponents.client5:httpclient5:5.4.1`, paired with an explicit `httpcore5:5.3.1` override since Spring Boot's parent BOM otherwise manages `httpcore5` down to an incompatible `5.2.5`). A custom `DnsResolver` (`TextExtractionService.SsrfSafeDnsResolver`) is registered directly on the `PoolingHttpClientConnectionManager`; it resolves the hostname exactly once, validates the result against private/loopback/link-local/multicast ranges, and that same resolution is what the connection manager uses to open the socket — there is no second, independent resolution for a DNS-rebinding attacker to race. Verified with a live fetch against `https://example.com/`, confirming real content is extracted (not the metadata stub) and `TextExtractionServiceTest` covers both rejection and the resolver's single-resolution contract.

### 3. Vulnerable `hono` dependency (dashboard) — confirmed not runtime-reachable, no action needed

`npm audit` in `apps/dashboard/` reports `hono <=4.12.24` with multiple advisories, including a CORS-wildcard-with-credentials reflection issue and AWS Lambda header handling bugs. Traced via `npm ls hono`: it is a transitive dependency of `shadcn` (dev-only CLI tool) → `@modelcontextprotocol/sdk` → `@hono/node-server`. It is not imported by any application code and never runs as part of the deployed dashboard. No fix required; re-check after any `shadcn`/MCP SDK upgrade.

## Medium

### 4. Prompt-injection guard is naive substring matching

`services/api/src/main/java/os/assurance/eu/api/evidence/PromptInjectionGuard.java:11-35`: matches a fixed list of English phrases (`"ignore previous"`, `"system prompt"`, etc.) case-insensitively, line by line. Trivially bypassed by paraphrase, non-English text, unicode tricks, or splitting a phrase across lines. `SECURITY.md` names prompt injection as a primary risk, so this control currently provides a false sense of mitigation rather than a real one.

**Fix direction**: keep as defense-in-depth only — do not rely on it as the primary control. The real mitigation is instruction/data separation in the LLM call (e.g., structured prompting that never lets retrieved evidence text occupy an instruction-privileged position) plus output validation on citations.

### 5. Outdated `postcss` via `next` (dashboard)

`npm audit` flags `postcss <8.5.10` (CSS stringify XSS, GHSA-qx2v-qp2m-jg93) pulled in transitively via `next`. The audit tool's suggested fix (downgrade `next`) is the wrong direction.

**Fix direction**: bump `postcss`/`next` minor versions manually rather than running `npm audit fix --force`.

## Low / Informational

- **`apps/dashboard/app/page.tsx:64`** — JSON-LD injected via `dangerouslySetInnerHTML`. Content is static SEO data today, no exploit path currently, but revisit if this content ever becomes dynamic or user-influenced.
- **Backend CORS** (`services/api/.../ApiWebConfig.java:12-20`) — `allowedHeaders("*")` is permissive. Acceptable while origins are localhost-only; tighten before adding production origins.
- **`apps/web/app.js`** — properly escapes all dynamic content via `escapeHtml()` before `innerHTML` writes, including RAG answer/citation rendering. No XSS found.
- **Solid, no action needed**: Eval callback HMAC verification (`EvalCallbackSignatureVerifier`) — mandatory secret enforced at startup, constant-time comparison, timestamp replay window, role-gated. JPA tenant scoping — all repository finders are tenant-scoped except two intentional, safe exceptions (API-key-hash lookup that derives tenant, and the cross-tenant worker dispatch claim query that immediately re-fetches tenant-scoped). No string-concatenated/native SQL injection risk found. No hardcoded secrets in source or properties files; `EVAL_CALLBACK_SECRET` / `DATABASE_PASSWORD` are externalized via env vars and fail closed on blank values.

## Not covered in this pass

- `pom.xml` dependency CVE lookup against a vulnerability database.
- Full controller-by-controller authZ sweep beyond eval-runs (release gate approval, evidence pack export, and manual eval triggers were spot-checked, not exhaustively swept).
