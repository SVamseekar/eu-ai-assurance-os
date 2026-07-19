# Part 7 — Evidence Pack PDF + Deterministic Seal

> **For agentic workers:** Keep JSON pack as primary API; PDF is export variant.

**Goal:** PRD §6 JSON pack remains; Phase 6 adds **PDF** and a **deterministic, traceable seal** (PRD §7).

**Depends on:** Part 5 (controls + registry fields in pack); Part 6 preferred (include chain head).

---

### Task 7.1: Deterministic JSON seal

- [ ] **Step 1:** Define pack payload schema version `evidence_pack_version: "1.0"`.
- [ ] **Step 2:** Canonical JSON (sorted keys) → `content_sha256`.
- [ ] **Step 3:** Response includes:
  - `generatedAt`
  - `contentSha256`
  - `generator` (service version / git commit if available)
  - optional `auditChainHead`
- [ ] **Step 4:** Same system state → same hash (test with fixed clocks via injectable clock).
- [ ] **Step 5:** Audit event `evidence_pack.exported` includes contentSha256.

### Task 7.2: PDF export

- [ ] **Step 1:** Dependency: OpenPDF or Apache PDFBox (prefer well-maintained, pure Java).
- [ ] **Step 2:** `GET /api/v1/systems/{id}/evidence-pack.pdf`  
  OR `Accept: application/pdf` on existing route — pick one; document in API.md.
- [ ] **Step 3:** PDF sections: system identity, risk + controls summary, evals, contracts, approvals, audit excerpt, seal footer (sha256).
- [ ] **Step 4:** Filename `evidence-pack-{systemId}-{date}.pdf`.
- [ ] **Step 5:** Integration test: non-empty PDF magic bytes `%PDF`.

### Task 7.3: Dashboard export UX

- [ ] **Step 1:** Systems/command UI: “Export JSON” + “Export PDF” buttons.
- [ ] **Step 2:** Download via proxy with auth cookies.
- [ ] **Step 3:** Show content SHA-256 after export (toast or modal).

### Task 7.4: API docs

- [ ] **Step 1:** Update `docs/API.md` with pack schema + PDF route.
- [ ] **Step 2:** Update PRD note: JSON required; PDF available (Part 11).

### Done when

- [ ] JSON pack sealed and deterministic under test  
- [ ] PDF export works authenticated  
- [ ] Dashboard can download both  
- [ ] Audit records export with hash  
