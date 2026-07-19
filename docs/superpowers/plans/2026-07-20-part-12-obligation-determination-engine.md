# Part 12 — Obligation Determination Engine (Assisted)

> **For agentic workers:** This is **not** a law firm or notified-body substitute. Product must always label outputs as **assisted obligation mapping**, not legal advice or final determination.

**Goal:** Deliver a structured **obligation / applicability determination engine** that was formerly PRD §4 “out of scope,” now **in program** as Phase 7 product expansion.

**Depends on:** Part 5 (controls + risk metadata), Part 7 (evidence pack can attach determination snapshot).

---

## Product framing (mandatory UX + legal copy)

| Allowed wording | Forbidden wording |
|---|---|
| “Suggested applicability / obligation map” | “You are compliant” |
| “Assisted determination (not legal advice)” | “Legal determination” as final truth |
| “Requires human legal review” | “Certified under the EU AI Act” |
| “Based on questionnaire + control catalog” | “Official conformity assessment” |

Landing FAQ, determination UI, and export header must include the disclaimer.

---

### Task 12.1: Domain model

- [x] **Step 1:** Flyway tables:
  - `obligation_rules` — code, title, description, legal_refs (Art/Annex text), applies_when JSON (conditions), severity, version
  - `determination_runs` — id, tenant_id, system_id, questionnaire_json, result_json, status, created_by, created_at
  - `determination_obligations` — run_id, rule_code, applicability (APPLICABLE|NOT_APPLICABLE|UNCERTAIN), rationale
- [x] **Step 2:** Seed v1 rule pack from existing control categories + high-risk indicators (HIGH risk, essential services, biometric flags as questionnaire inputs—not invented law).
- [x] **Step 3:** Version field `ruleset_version` on every run for auditability.

### Task 12.2: Questionnaire API

- [x] **Step 1:** `GET /api/v1/determination/questionnaire` — versioned questions (sector, users affected, decision impact, biometric, employment, essential private service, human in loop, etc.).
- [x] **Step 2:** `POST /api/v1/systems/{id}/determination/runs` — body answers → evaluate rules → store run.
- [x] **Step 3:** `GET .../determination/runs/{runId}` — full result + disclaimer.
- [x] **Step 4:** Rule engine: pure Java evaluator over JSON conditions (no LLM required for v1; optional LLM narrative later).
- [x] **Step 5:** Tests for HIGH insurance triage → expected obligation set; MINIMAL chatbot → reduced set; uncertain paths.

### Task 12.3: Link to controls + risk

- [x] **Step 1:** Applicable obligations map to control codes (Part 5); auto-open system_controls REVIEW for new applicable obligations.
- [x] **Step 2:** Optional suggestion to reclassify risk (never auto-change without human confirm).
- [x] **Step 3:** Audit events: `determination.run.completed`.

### Task 12.4: Dashboard UX

- [x] **Step 1:** Systems → “Obligation map” wizard.
- [x] **Step 2:** Results: Applicable / Uncertain / Not applicable lists + legal_refs + “human review required” banner.
- [x] **Step 3:** Export include in evidence pack (Part 7) as `determination` section.

### Task 12.5: Docs + claims

- [x] **Step 1:** PRD §4 updated: determination engine **in scope as assisted** (Part 12).
- [x] **Step 2:** Metrics canon: “Assisted obligation determination (ruleset vX)” — never “legal determination engine” alone without “assisted”.

### Done when

- [x] Deterministic rule evaluation with tests  
- [x] Disclaimer on every surface  
- [x] Linked to controls + pack  
- [x] No claim of legal finality  
