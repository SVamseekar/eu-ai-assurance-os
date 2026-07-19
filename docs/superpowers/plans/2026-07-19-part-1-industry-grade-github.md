# Part 1 — Industry-Grade GitHub Setup

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans.

**Goal:** Bring `eu-ai-assurance-os` GitHub operations to parity with WorkforceGuard AI (CI, protection, license, templates, accurate README).

**Reference:** `/Users/souravamseekarmarti/Projects/WorkforceGuard-AI/.github/`, `CONTRIBUTING.md`.

**Depends on:** Part 0 preferred (so CI validates complete tree).

---

### Task 1.1: LICENSE + community files

- [x] **Step 1:** Add `LICENSE` (recommend **Apache-2.0** or **MIT** — confirm with owner). → **MIT**
- [x] **Step 2:** Add `CONTRIBUTING.md` — branch naming (`feature/`, `fix/`, `chore/`), PR required, local `mvn test` + `npm run build`, no secrets.
- [x] **Step 3:** Add `.github/pull_request_template.md`, issue templates (bug/feature), optional `CODEOWNERS`.
- [x] **Step 4:** Add `SECURITY.md` GitHub-facing policy (link to `docs/SECURITY.md` + private report email).

### Task 1.2: CI workflows

- [x] **Step 1:** `.github/workflows/ci.yml`:
  - Job `api`: Java 17, `mvn -B test` in `services/api`
  - Job `dashboard`: Node 22, `npm ci`, `npx tsc --noEmit`, `npm run build` in `apps/dashboard`
  - Job `secret-scan`: gitleaks
- [x] **Step 2:** Optional path filters to skip unrelated jobs.
- [x] **Step 3:** Dependabot for `npm` (apps/dashboard) and `maven` (services/api).
- [x] **Step 4:** Ensure build does not require production secrets (use test defaults / empty `EVAL_CALLBACK_SECRET` for H2).

### Task 1.3: Branch protection & repo settings

- [ ] **Step 1:** Enable protection on `main`: require PR, require CI status checks, deny force-push. *(apply after first CI green on main)*
- [x] **Step 2:** Set homepage URL to `https://euassuranceai.souravamseekar.com`.
- [x] **Step 3:** Confirm topics remain accurate; add `license` once chosen.
- [x] **Step 4:** Document settings in `docs/` or CONTRIBUTING (solo-admin bypass OK).

### Task 1.4: README rewrite

- [x] **Step 1:** Lead with production dashboard + Spring API (not static prototype).
- [x] **Step 2:** Document: run API, run dashboard, env vars (`EVAL_CALLBACK_SECRET`, `ASSURANCE_API_BASE_URL`, `NEXT_PUBLIC_SITE_URL`, `NEXT_PUBLIC_GA_MEASUREMENT_ID`).
- [x] **Step 3:** Link live demo domain, phases, security audit, metrics canon (after Part 2).
- [x] **Step 4:** Keep `apps/web` as “legacy prototype” only.

### Task 1.5: Local git hygiene

- [x] **Step 1:** Expand `.gitignore` if needed (`.DS_Store` already; ensure `.vercel` stays ignored).
- [x] **Step 2:** Optional pre-commit (gitleaks + trailing whitespace) — mirror WorkforceGuard lightly. *(deferred; Gitleaks in CI is the gate)*
- [ ] **Step 3:** Tag release after Part 0 merge: `v0.5.0-phase5` (example).

### Done when

- [ ] PR to main fails if `mvn test` or dashboard build fails  
- [x] LICENSE + CONTRIBUTING present  
- [x] README describes real product  
- [ ] Branch protection enabled (or documented if org plan limits)  
