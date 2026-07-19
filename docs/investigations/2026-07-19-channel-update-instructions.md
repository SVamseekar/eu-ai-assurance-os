# Channel Update Instructions (Non-Code Assets)

**Date:** 2026-07-19  
**Audience:** Claude Chat / separate sessions editing CV, Portfolio, Pitch  
**Rule:** Do **not** apply until Part 0 (Phase 5 WIP) is merged and metrics re-measured. Numbers below are **pre-freeze estimates** from 2026-07-19 investigation; re-count on freeze day.

**Primary investigation:** `docs/investigations/2026-07-19-end-to-end-status-metrics-and-channel-alignment.md`

---

## Paths

| Asset | Path |
|---|---|
| CV DOCX | `/Users/souravamseekarmarti/Documents/Marti_Soura_Vamseekar_CV.docx` |
| CV PDF | `/Users/souravamseekarmarti/Documents/Marti_Soura_Vamseekar_CV.pdf` |
| Portfolio project entry | `/Users/souravamseekarmarti/Projects/Portfolio/martisouravamseekar-portfolio/src/data/projects.ts` |
| Pitch deck | `/Users/souravamseekarmarti/Downloads/MSV_AI_Labs_Pitch_Deck.pdf` |
| Landing (code session) | `eu-ai-assurance-os` Part 3 plan |

---

## A. CV — replace EU AI Assurance OS block

### Remove / fix these claims

- FAISS  
- Next.js 14  
- Flyway V1–V6 only  
- “HMAC-SHA-256 signed audit event stream”  
- “automated … risk classification” (as ML automation)  
- Risk tiers “Unacceptable, High, Limited, Minimal” without Prohibited mapping  

### Paste-ready replacement (English)

```text
EU AI Assurance OS — Governance Control Plane for EU AI Act  ·  GitHub
Spring Boot 3.3 · Java 17 · Next.js 16 · pgvector HNSW · DJL/ONNX embeddings · Multi-tenant

• Governance: AI system registry with guided EU AI Act risk classification
  (Minimal / Limited / High / Prohibited), open-gap tracking, and release decisions
  PASS / REVIEW / BLOCKED from evidence coverage, eval thresholds, and data-contract status.
• Evidence & evals: Cited-evidence RAG (DJL + ONNX Runtime all-MiniLM-L6-v2; pgvector HNSW
  on Postgres); durable eval worker (select-for-update-skip-locked); HMAC-SHA-256 signed
  eval-result callbacks; Evidence Pack JSON for audit-ready technical documentation.
• Architecture: Spring Boot API with Flyway migrations (V1–V9+), tenant-scoped JPA +
  JWT/API-key auth, Next.js dashboard (lineage DAG, contracts, approvals, audit log).
  OAuth/SSO on roadmap.
```

After Part 0 merge, append if true: “workflow notifications and reviewer assignment.”

Also fix **summary line** if it over-indexes wrong tech for this product.

---

## B. Portfolio — `projects.ts` EU AI Assurance OS entry

### Current problems (2026-07-19)

- “automated EU AI Act risk classification”  
- Flyway **V1–V6** stale  
- Stack OK on DJL/ONNX but period “2024 – Present” questionable vs repo created 2026-06-05  
- “HMAC-SHA-256 signed audit event stream” overstates audit (HMAC is eval callbacks)

### Suggested `metrics` array

```ts
metrics: [
  "AI system registry with guided EU AI Act risk classification (Minimal / Limited / High / Prohibited)",
  "Cited-evidence RAG: DJL + ONNX Runtime (all-MiniLM-L6-v2), pgvector HNSW; PASS / REVIEW / BLOCKED release gates",
  "HMAC-SHA-256 signed eval-result callbacks; append-only audit ledger; Evidence Pack JSON export",
  "Spring Boot 3.3 backend: Flyway V1–V9 (+ V10 when merged), multi-tenant JPA, JWT + API keys, eval worker queue",
  "Next.js 16 dashboard with interactive DAG lineage graph (@xyflow/react), contracts, approvals",
],
```

### Suggested stack hygiene

Keep: Java 17, Spring Boot 3.3, PostgreSQL, pgvector, Next.js 16, DJL, ONNX Runtime, HuggingFace tokenizers, AWS S3, Apache Tika, @xyflow/react  

Avoid adding: FAISS  

### Period

Prefer `"2026 – Present"` unless you have independent proof of 2024 production traffic.

---

## C. Pitch deck — EU AI Assurance OS slide

### Keep

- Control plane narrative  
- Aug 2026 enforcement market timing  
- Spring Boot 3.3 · Next.js 16 · pgvector HNSW · ONNX/DJL  

### Change

| Current | Corrected |
|---|---|
| Key metric “HMAC-256 Tamper-evident audit event streams” | “HMAC-SHA-256 eval-result callbacks” + “Append-only audit ledger” |
| “automated risk classification” | “Guided risk classification + release gates” |
| “Live since 2024” | “Live 2026” / “Public product 2026” (unless proven) |
| Thin metrics only | Optional: ~40 API endpoints · 100+ automated tests · 6 capability modules |

### Do not invent

Revenue, customer counts, “X enterprises in production” without data.

---

## D. Landing (code) — for Part 3 implementer

- Metrics strip from canon only  
- Enterprise refunds (order form / SOW) — **not** Aequitas consumer “no partial refunds” alone  
- Primary CTA: Request demo  
- Disclaimer: not legal certification  

---

## E. Consistency checklist (all four channels)

- [ ] Same product one-liner  
- [ ] Same risk class names  
- [ ] Same release decision names  
- [ ] Same embedding stack (DJL/ONNX, not FAISS)  
- [ ] Same auth story (JWT/API keys; OAuth only after Part 4)  
- [ ] Same Flyway range  
- [ ] Same Next.js major version  
- [ ] Same live URL `https://euassuranceai.souravamseekar.com`  
- [ ] Same GitHub URL  

---

*Generated 2026-07-19. Re-measure before final CV/pitch print.*
