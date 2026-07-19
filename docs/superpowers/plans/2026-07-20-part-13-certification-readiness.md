# Part 13 — Certification Readiness (Not Automated Certification)

> **For agentic workers:** Product produces a **readiness score + gap report** toward conformity documentation. It does **not** issue certificates or declare conformity.

**Goal:** Cover former PRD §4 “automated certification” demand as **certification readiness automation**.

**Depends on:** Parts 5, 6, 7, 12.

---

## Product framing

| Ship | Do not ship |
|---|---|
| Readiness % and gap list | “Certified” badge as legal status |
| Checklist vs Annex-style evidence themes | Notified-body attestation |
| One-click readiness report PDF/JSON | Claiming Regulation conformity |

---

### Task 13.1: Readiness model

- [ ] **Step 1:** Define readiness dimensions: risk classified, controls coverage, evidence indexed, eval gate, contracts healthy, approvals complete, oversight evidence, determination run present, audit chain valid.
- [ ] **Step 2:** Weighted score 0–100 with config in `assurance.certification-readiness.*`.
- [ ] **Step 3:** Gaps as structured list `{ code, severity, message, remediationHint }`.

### Task 13.2: API

- [ ] **Step 1:** `GET /api/v1/systems/{id}/certification-readiness`
- [ ] **Step 2:** `POST /api/v1/systems/{id}/certification-readiness/export` → JSON report + optional PDF (reuse pack tooling).
- [ ] **Step 3:** Never return field `certified: true` for legal conformity — use `readinessStatus: NOT_READY|READY_FOR_REVIEW|GAPS`.
- [ ] **Step 4:** Tests for blocked system → NOT_READY with gaps; full happy path → READY_FOR_REVIEW.

### Task 13.3: Dashboard

- [ ] **Step 1:** Command/system card: readiness ring + top 3 gaps.
- [ ] **Step 2:** Full page/section with dimension breakdown.
- [ ] **Step 3:** CTA: open missing evidence / controls / workflows.

### Task 13.4: Landing / metrics wording

- [ ] **Step 1:** Marketing: “Certification **readiness** automation” only.
- [ ] **Step 2:** Update channel packet accordingly.

### Done when

- [ ] Score + gaps API + UI  
- [ ] Export report  
- [ ] Zero “you are certified” strings in product  
