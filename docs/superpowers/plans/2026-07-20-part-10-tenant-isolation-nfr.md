# Part 10 — Tenant Isolation Hardening + NFR Measurement Hooks

> **For agentic workers:** Security-sensitive; prefer tests that fail closed.

**Goal:** PRD §7 tenant isolation + ROADMAP “Tenant isolation hardening”; leave latency/uptime as **measurable**, not fake-claimed.

**Depends on:** Part 0 (auth model stable).

---

### Task 10.1: Cross-tenant isolation regression suite

- [x] **Step 1:** Test harness: two tenants A/B, users, systems, evidence, contracts, workflows, audit.
- [x] **Step 2:** For each major GET/list/mutate endpoint, authenticate as A and attempt B’s IDs → 404 or 403 (never 200 with B data).
- [x] **Step 3:** API key for A cannot read B.
- [x] **Step 4:** Eval callback remains signature-gated (existing); ensure tenant cannot forge cross-tenant run IDs.
- [x] **Step 5:** Class name e.g. `TenantIsolationTest`; run in CI.

### Task 10.2: Authorization completeness

- [x] **Step 1:** Role matrix document: which roles can approve, override, export pack, manage controls.
- [x] **Step 2:** Enforce via `TenantAuthorizationService` on mutating endpoints missing checks.
- [x] **Step 3:** Tests for FORBIDDEN paths.

### Task 10.3: Remove residual trust gaps

- [x] **Step 1:** Re-audit `TenantContextFilter` unauthenticated paths allowlist (keep health, auth, jwks only).
- [x] **Step 2:** Confirm Spring Security permitAll is intentional and filter is never bypassed.
- [x] **Step 3:** Dashboard: no client-supplied tenant headers (already removed — regression test / grep check).
- [x] **Step 4:** Optional Next middleware hard-gate for `/command` etc. when no session cookie → redirect `/login` (defense in depth).

### Task 10.4: Encryption / secrets NFR documentation

- [x] **Step 1:** Document TLS termination expectations (Vercel / reverse proxy).
- [x] **Step 2:** Document at-rest: Postgres disk encryption is provider responsibility; secrets in env/secret manager.
- [x] **Step 3:** No secrets in repo (gitleaks Part 1).

### Task 10.5: Latency measurement hooks (not false compliance)

- [x] **Step 1:** Micrometer timers (Part 8 overlap OK) on registry + evidence query.
- [x] **Step 2:** `docs/NFR.md` or section: how to query p95; target values from PRD §7 as **targets**, not certified until measured in prod.
- [x] **Step 3:** Uptime 99.9% as ops SLO — document monitoring approach (UptimeRobot / Vercel / Actuator).

### Done when

- [x] Cross-tenant suite green in CI  
- [x] Role matrix documented + enforced  
- [x] Middleware or equivalent session gate for app shell  
- [x] NFR doc honest about targets vs measured  
