# Part 8 — CI/CD Release Gate Integration + Observability

> **For agentic workers:** Part 1 creates basic CI; this part adds **productized** release-gate-for-CI and observability.

**Goal:**  
1) Every PR runs full quality gates.  
2) External CI/CD can call Assurance OS for a machine-readable release decision (EvalForge / PRD §10).  
3) Operators have metrics + a minimal observability surface (Phase 6).

**Depends on:** Part 1 (CI skeleton), Part 5 (stable gate + controls).

---

### Task 8.1: Harden GitHub CI (extends Part 1)

- [ ] **Step 1:** Jobs: `api-test`, `dashboard-build`, `secret-scan`, optional `acceptance` (`ClaimsTriageAcceptanceTest`).
- [ ] **Step 2:** Cache Maven + npm.
- [ ] **Step 3:** Fail on test failure; upload reports as artifacts.
- [ ] **Step 4:** Branch protection requires these checks.

### Task 8.2: Release gate CI API (product feature)

Enable pipelines to block deploys on BLOCKED systems.

- [ ] **Step 1:** `GET /api/v1/systems/{id}/release-gate` already exists — add:
  - `GET /api/v1/ci/release-gate?systemId=` or by `externalRef` if we add one
  - Response machine-friendly: `{ decision, blockers[], evalScore, evidenceCoverage, content? }`
  - Exit-code oriented docs: PASS=0, REVIEW=2, BLOCKED=1 for CLI
- [ ] **Step 2:** CLI script `scripts/ci-release-gate.sh`:
  - Inputs: `ASSURANCE_API_BASE`, `API_KEY` or token, `SYSTEM_ID`
  - Curl gate; exit non-zero on BLOCKED
- [ ] **Step 3:** Example GitHub Action workflow `release-gate-example.yml` (workflow_call or docs-only sample).
- [ ] **Step 4:** Auth: API key recommended for CI bots (document).
- [ ] **Step 5:** Tests for response contract stability.

### Task 8.3: Observability — backend

- [ ] **Step 1:** Confirm Actuator: health, metrics, prometheus if acceptable (`micrometer-registry-prometheus`).
- [ ] **Step 2:** Metrics already for eval runs — add:
  - `assurance.release_gate.decision` counter tags decision
  - `assurance.evidence.query.latency`
  - `assurance.audit.append`
  - `assurance.auth.login.failures`
- [ ] **Step 3:** Correlation ID filter already present — ensure MDC in logs.
- [ ] **Step 4:** Document scrape endpoint in ops README.

### Task 8.4: Observability — dashboard surface

- [ ] **Step 1:** Lightweight `/command` or `/ops` widgets (admin): recent gate decisions counts, open workflows, open breaches — from existing APIs (no full Grafana required).
- [ ] **Step 2:** API status pill already exists — ensure production health proxied carefully (no secret leak).
- [ ] **Step 3:** Optional public status: out of scope unless simple `/api/health` already public.

### Task 8.5: Latency NFR hooks (PRD §7)

- [ ] **Step 1:** Timed metrics on registry list/get and evidence query.
- [ ] **Step 2:** Document how to read p95 from Prometheus/Actuator; do **not** fake compliance without data.
- [ ] **Step 3:** Optional CI smoke asserts p95 not measured in unit tests — only that timers exist.

### Done when

- [ ] CI green required on main  
- [ ] `scripts/ci-release-gate.sh` blocks on BLOCKED  
- [ ] Prometheus/Actuator metrics for gate + RAG + auth  
- [ ] Operator-visible summary in dashboard  
