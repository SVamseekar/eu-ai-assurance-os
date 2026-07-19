# Part 0 — Finish Phase 5 WIP (STOP-GATE)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans. Checkbox steps for tracking.

**Goal:** Commit and ship the uncommitted Phase 5 completion (notifications, reviewer assignment, oversight evidence, approvals UI) so metrics and ROADMAP “complete” are true on `origin/main`.

**Architecture:** Existing `workflow` package + Flyway V10 + dashboard approvals page.

**Tech stack:** Spring Boot 3.3, Flyway, Next.js 16, TanStack Query.

**Depends on:** Nothing — **this is the first plan to execute.**

---

### Task 0.1: Inventory and branch

- [x] **Step 1:** Confirm dirty files match investigation inventory (workflow + V10 + approvals UI + docs).
- [x] **Step 2:** Create branch `feature/phase5-workflow-completion` from up-to-date `main` (stash or commit only WIP-related files; do not mix GitHub hygiene into this PR).
- [x] **Step 3:** Ensure V10 is the only new migration and is sequential after V9.

### Task 0.2: Backend verification

- [x] **Step 1:** `cd services/api && mvn test` — all green, especially `ApprovalWorkflowServiceTest`.
- [x] **Step 2:** Manually verify V10 SQL: `assigned_reviewer_id`, `oversight_evidence`, `notification_sent_at`, `workflow_notifications` + indexes.
- [x] **Step 3:** Confirm controller exposes notification endpoints and stage approve requires oversight when legal stage needs it.
- [x] **Step 4:** Confirm `BootstrapData` seeds roles/users needed for assignment demos without breaking postgres profile.

### Task 0.3: Frontend verification

- [x] **Step 1:** Types in `lib/types.ts` match API notification payload.
- [x] **Step 2:** `lib/api.ts` clients for open/mine/notifications.
- [x] **Step 3:** Approvals page shows open, assigned, notifications; action modal supports oversight evidence text.
- [x] **Step 4:** Mock data path still works offline.

### Task 0.4: Docs alignment (code-adjacent only)

- [x] **Step 1:** `docs/API.md` documents notification + stage fields.
- [x] **Step 2:** `docs/ROADMAP.md` Phase 5 complete **only if** tests + UI verified.
- [x] **Step 3:** Do **not** yet rewrite root README for full product story (Part 1) unless needed for accuracy on workflows.

### Task 0.5: PR and merge

- [ ] **Step 1:** Commit with conventional message, e.g. `feat(workflow): complete phase 5 notifications and reviewer assignment`.
- [ ] **Step 2:** Open PR → review checklist (migration, tests, UI).
- [ ] **Step 3:** Merge to `main`; deploy dashboard if Vercel auto-deploys; note API deploy path separately (if API not on Vercel, document host).
- [ ] **Step 4:** Mark investigation stop-gate complete; proceed to Part 1 or 2.

### Done when

- [ ] `origin/main` contains V10 + notification classes + green tests  
- [ ] Local working tree clean for this scope  
- [ ] Live/staging API can migrate V10 without error  

### Session log (2026-07-20)

- Branch: `feature/phase5-workflow-completion`
- `mvn test`: 101 run, 0 failures, 1 skipped; Flyway applies through V10 on H2
- `ApprovalWorkflowServiceTest`: 14 tests green (incl. legal oversight evidence)
- Dashboard `npx tsc --noEmit`: green
- Offline approvals mock: `MOCK_OPEN_WORKFLOWS` / `MOCK_MY_WORKFLOWS` / `MOCK_NOTIFICATIONS` + placeholderData
