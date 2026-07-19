# Part 10 — Tenant Isolation Hardening + NFR Measurement Hooks

> **For agentic workers:** Security-sensitive; prefer tests that fail closed.

**Goal:** PRD §7 tenant isolation + ROADMAP “Tenant isolation hardening”; leave latency/uptime as **measurable**, not fake-claimed.

**Depends on:** Part 0 (auth model stable).

---

### Task 10.1: Cross-tenant isolation regression suite

- [ ] **Step 1:** Test harness: two tenants A/B, users, systems, evidence, contracts, workflows, audit.
- [ ] **Step 2:** For each major GET/list/mutate endpoint, authenticate as A and attempt B’s IDs → 404 or 403 (never 200 with B data).
- [ ] **Step 3:** API key for A cannot read B.
- [ ] **Step 4:** Eval callback remains signature-gated (existing); ensure tenant cannot forge cross-tenant run IDs.
- [ ] **Step 5:** Class name e.g. `TenantIsolationTest`; run in CI.

### Task 10.2: Authorization completeness

- [ ] **Step 1:** Role matrix document: which roles can approve, override, export pack, manage controls.
- [ ] **Step 2:** Enforce via `TenantAuthorizationService` on mutating endpoints missing checks.
- [ ] **Step 3:** Tests for FORBIDDEN paths.

### Task 10.3: Remove residual trust gaps

- [ ] **Step 1:** Re-audit `TenantContextFilter` unauthenticated paths allowlist (keep health, auth, jwks only).
- [ ] **Step 2:** Confirm Spring Security permitAll is intentional and filter is never bypassed.
- [ ] **Step 3:** Dashboard: no client-supplied tenant headers (already removed — regression test / grep check).
- [ ] **Step 4:** Optional Next middleware hard-gate for `/command` etc. when no session cookie → redirect `/login` (defense in depth).

### Task 10.4: Encryption / secrets NFR documentation

- [ ] **Step 1:** Document TLS termination expectations (Vercel / reverse proxy).
- [ ] **Step 2:** Document at-rest: Postgres disk encryption is provider responsibility; secrets in env/secret manager.
- [ ] **Step 3:** No secrets in repo (gitleaks Part 1).

### Task 10.5: Latency measurement hooks (not false compliance)

- [ ] **Step 1:** Micrometer timers (Part 8 overlap OK) on registry + evidence query.
- [ ] **Step 2:** `docs/NFR.md` or section: how to query p95; target values from PRD §7 as **targets**, not certified until measured in prod.
- [ ] **Step 3:** Uptime 99.9% as ops SLO — document monitoring approach (UptimeRobot / Vercel / Actuator).

### Done when

- [ ] Cross-tenant suite green in CI  
- [ ] Role matrix documented + enforced  
- [ ] Middleware or equivalent session gate for app shell  
- [ ] NFR doc honest about targets vs measured  
