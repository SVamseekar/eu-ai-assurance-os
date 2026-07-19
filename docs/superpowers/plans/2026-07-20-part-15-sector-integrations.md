# Part 15 — Sector Integrations (First Verticals)

> **For agentic workers:** “Complex sector-specific integrations” → ship **2–3 deep vertical packs** + extension SPI, not every industry at once.

**Goal:** Bring sector integrations **into the program** with a plugin model and first verticals aligned to PRD use case + EU AI Act high-risk themes.

**Depends on:** Parts 5, 12; Part 14 optional for sector tags.

---

## First verticals (v1)

| Sector pack | Why |
|---|---|
| **Insurance / claims** | PRD primary use case (Claims Triage AI) |
| **HR / employment** | Common high-risk Annex-style theme |
| **Financial services / KYC** | Already in mock dashboard demos |

Each pack = controls overlay + questionnaire defaults + sample evidence templates + optional connector stubs.

---

### Task 15.1: Integration SPI

- [x] **Step 1:** Java interface `SectorPack`:
  - `id()`, `displayName()`
  - `extraControls()` / control code overlays
  - `questionnaireDefaults()`
  - `sampleEvidenceTemplates()`
- [x] **Step 2:** Spring `SectorPackRegistry` loads packs via config `assurance.sector.packs=insurance,hr,finance`.
- [x] **Step 3:** Tests: enable insurance pack → extra controls attached on system with sector=INSURANCE.

### Task 15.2: Insurance pack (depth)

- [x] **Step 1:** Controls/obligations for claims automation, fairness, human review of adverse decisions.
- [x] **Step 2:** Sample DPIA/model-card templates (markdown in `resources/sector/insurance/`).
- [x] **Step 3:** Optional webhook stub: `POST /api/v1/integrations/insurance/claims-model-register` (maps external model id → system registry fields).
- [x] **Step 4:** Dashboard: sector selector on system create; shows pack badge.

### Task 15.3: HR pack

- [x] **Step 1:** Hiring/ranking transparency + human oversight controls.
- [x] **Step 2:** Templates + questionnaire defaults.
- [x] **Step 3:** Tests.

### Task 15.4: Finance / KYC pack

- [x] **Step 1:** Fraud/KYC assistant controls; logging intensity.
- [x] **Step 2:** Templates + tests.

### Task 15.5: Connector stubs (integration boundary)

- [x] **Step 1:** Generic `IntegrationConnector` interface: `pushReleaseDecision`, `pullModelInventory` — no-op or log for v1.
- [x] **Step 2:** Document how a real Workday/Guidewire/etc. connector would plug in.
- [x] **Step 3:** Do not claim live production connectors to proprietary vendors without OAuth apps.

### Task 15.6: Docs / metrics

- [x] **Step 1:** ROADMAP Phase 7 “Sector packs: insurance, HR, finance”.
- [x] **Step 2:** Metrics: “3 sector packs + SPI” not “all industries integrated”.

### Done when

- [x] SPI + 3 packs loadable  
- [x] Insurance depth meets Claims Triage story  
- [x] Connector extension points documented  
- [x] Honest marketing claims  
