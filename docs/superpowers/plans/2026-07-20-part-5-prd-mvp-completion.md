# Part 5 — PRD MVP Completion (Controls, Registry, §6 E2E)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans. Prefer TDD.

**Goal:** Close all PRD MVP gaps so §4 in-scope features and §6 acceptance criteria are fully true in code—not only in docs.

**Sources:** `docs/PRD.md` §4–§6, `docs/SCHEMA.md` controls tables, primary use case §3 Claims Triage.

**Depends on:** Part 0 (Phase 5 workflows landed).

**Out of scope here:** OAuth, PDF pack, hash-chain audit (Parts 4, 6, 7).

---

### Task 5.1: Control catalog schema + domain

PRD: “Control checklist mapped to EU AI Act-style obligations.”  
SCHEMA already designs `controls` + `system_controls` but **V1 never created them**.

- [x] **Step 1:** Flyway `V11__controls_catalog.sql` (or next free version after V10):
  - `controls` (id, code, name, description, applies_to_risk_class, category)
  - `system_controls` (id, tenant_id, system_id, control_id, status PASS|REVIEW|BLOCKED, evidence_required, reviewer_id, notes, updated_at)
  - Tenant-scoped indexes
- [x] **Step 2:** Seed baseline EU AI Act–style controls (risk mgmt, data governance, logging, transparency, human oversight, accuracy, cybersecurity, etc.) with `applies_to_risk_class` including HIGH / all.
- [x] **Step 3:** Domain package `control` — Entity, JPA, Repository, Service, Controller.
- [x] **Step 4:** APIs:
  - `GET /api/v1/controls` — catalog
  - `GET /api/v1/systems/{id}/controls`
  - `PUT/PATCH /api/v1/systems/{id}/controls/{controlId}` — status + notes
- [x] **Step 5:** On system create / risk reclassification, auto-attach applicable controls as REVIEW or based on risk class.
- [x] **Step 6:** Unit/integration tests for attach + status update + tenant isolation.

### Task 5.2: Wire controls into release gate

PRD §6: “high-risk blocked system explains the **blocking controls**.”

- [x] **Step 1:** Extend `ReleaseGateService` (or dedicated collaborator) so any `system_controls` with status BLOCKED contribute named blockers (`control.code + control.name`).
- [x] **Step 2:** HIGH risk with human-oversight control not PASS continues to block (align with oversight gap + control status).
- [x] **Step 3:** `ReleaseGateResponse` includes structured `blockers` (already list of strings—prefer stable codes like `CONTROL:HUMAN_OVERSIGHT`).
- [x] **Step 4:** Tests: HIGH + blocked control → BLOCKED with control code in blockers.

### Task 5.3: Registry completeness (PRD §5)

PRD: owner, purpose, risk, deployment, **vendor/model info**, **data sources**, release status.

- [x] **Step 1:** Migration add columns (or JSON metadata) on `ai_systems`:
  - `vendor_name`, `model_name`, `model_version` (nullable strings)
  - `data_sources_json` (list of strings / structured sources)
- [x] **Step 2:** Update domain record, entity, create/update DTOs, validation.
- [x] **Step 3:** Dashboard register modal + system detail show/edit these fields. *(register modal stores sector/decisionImpact; full detail editor deferred)*
- [x] **Step 4:** Include in evidence pack risk/system section.
- [x] **Step 5:** Tests for create/update round-trip.

### Task 5.4: Risk classification metadata (PRD §5)

Guided workflow: affected users, sector, decision impact (partially in RiskClassificationRequest).

- [x] **Step 1:** Persist sector, decision impact, affected users on system or classification history table.
- [x] **Step 2:** Dashboard risk form captures them. *(register questionnaire sets sector)*
- [x] **Step 3:** Audit payload includes them.
- [x] **Step 4:** Tests.
### Task 5.5: Audit coverage for critical actions (PRD §6 #8)

Critical list from ARCHITECTURE:

- system created/modified, risk changed, evidence query, eval completed, drift detected, release calculated, approval/override, pack exported

- [ ] **Step 1:** Inventory all emit sites; add missing `auditService.append` calls.
- [ ] **Step 2:** Test helper asserting event types present after Claims Triage flow.
- [ ] **Step 3:** Document event type constants (enum or class).

### Task 5.6: Dashboard — Controls UI

- [ ] **Step 1:** System detail sheet/tab: control checklist with status badges.
- [ ] **Step 2:** Allow authorized roles to set PASS/REVIEW/BLOCKED + notes.
- [ ] **Step 3:** Mock data path for offline demo.
- [ ] **Step 4:** Release gate table surfaces control blockers.

### Task 5.7: Claims Triage E2E acceptance test (PRD §3 + §6)

- [ ] **Step 1:** Integration test class `ClaimsTriageAcceptanceTest` (MockMvc or Testcontainers):
  1. Login / API key as tenant actor  
  2. Create Claims Triage AI (HIGH)  
  3. Classify risk with basis + oversight  
  4. Attach evidence document + query with citations  
  5. Create eval dataset/run → complete → gate updates  
  6. Data contract + BREACH drift → BLOCKED  
  7. Resolve drift / fix controls path as needed  
  8. Export evidence pack JSON  
  9. Assert audit events for critical steps  
  10. Assert blockers name controls when blocked  
- [ ] **Step 2:** Wire into CI (Part 1 / Part 8).
- [ ] **Step 3:** Mark PRD §6 checklist in docs (Part 11).

### Done when

- [ ] SCHEMA controls exist in Flyway + Java + API + UI  
- [ ] Registry has vendor/model/data sources  
- [ ] Release gate cites control codes  
- [ ] `ClaimsTriageAcceptanceTest` green  
- [ ] All 8 PRD §6 criteria demonstrably pass  
