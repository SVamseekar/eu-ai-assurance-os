# Part 11 — Documentation Alignment (PRD, ROADMAP, README, Architecture)

> **For agentic workers:** Keep docs honest; never mark Phase complete without code on main.

**Goal:** Docs match product; PRD is clean; roadmap reflects reality; README is production-grade.

**Depends on:** Feature parts as they land; can fix typos immediately.

---

### Task 11.1: PRD cleanup

- [x] **Step 1:** Fix line 1 corruption (`see#` → `# Product Requirements Document`).
- [x] **Step 2:** Add status footer: “MVP acceptance verified by `ClaimsTriageAcceptanceTest` (date).”
- [x] **Step 3:** Clarify §4: production SSO is out of MVP; delivered as Enterprise/Phase 6 (Part 4).
- [x] **Step 4:** §6 criteria — link to test class / API routes.
- [x] **Step 5:** Keep out-of-scope list; do not claim certification.

### Task 11.2: ROADMAP honesty

- [x] **Step 1:** Phase 0–4: complete (verify).
- [x] **Step 2:** Phase 5: complete **only after** Part 0 on main.
- [x] **Step 3:** Phase 6: checklist with status per item (SSO, audit, PDF, CI gate, observability, docker/terraform, isolation).
- [x] **Step 4:** Note Phase 2 leftovers resolved or deferred with links to Part 9.

### Task 11.3: README rewrite

- [x] **Step 1:** Lead with live URL + Next.js dashboard + Spring Boot API.
- [x] **Step 2:** Quickstart: API H2, API Postgres compose, dashboard dev.
- [x] **Step 3:** Env table; security notes; link DEPLOYMENT.md, PRD, ROADMAP, METRICS_CANONICAL.
- [x] **Step 4:** Legacy `apps/web` demoted.
- [x] **Step 5:** License badge once Part 1 lands.

### Task 11.4: ARCHITECTURE / SCHEMA / API / SECURITY

- [x] **Step 1:** SCHEMA.md: match actual Flyway (including controls after Part 5, hash columns after Part 6).
- [x] **Step 2:** ARCHITECTURE.md: audit store = hash-chained Postgres (not mythical external only); worker queue = DB not Kafka-required.
- [x] **Step 3:** API.md: all new endpoints (controls, PDF pack, OAuth, verify audit, CI gate).
- [x] **Step 4:** SECURITY.md: SSO status, chain secret, session cookies, remaining threats.

### Task 11.5: Metrics + channel freeze

- [x] **Step 1:** After Waves B–D, re-measure and freeze `docs/METRICS_CANONICAL.md` (Part 2).
- [x] **Step 2:** Refresh `docs/investigations/2026-07-19-channel-update-instructions.md` with final numbers.
- [x] **Step 3:** Hand off CV / Portfolio / Pitch to other sessions. *(Part 16 — not this part)*

### Task 11.6: Program completion record

- [x] **Step 1:** Write `docs/investigations/2026-07-20-roadmap-completion-signoff.md` with:
  - PRD §6 matrix pass/fail  
  - Phase 6 matrix pass/fail  
  - Live URLs checked  
  - Known residual risks  
- [ ] **Step 2:** Tag release `v1.0.0` (or `v0.9.0` if OAuth still optional) after signoff. *(Recommend `v0.9.0` after merge; OAuth prod smoke still open)*

### Done when

- [x] No doc claims features that are not on main  
- [x] PRD readable and accurate  
- [x] ROADMAP Phase 6 items checked off with evidence  
- [x] Signoff investigation filed  
