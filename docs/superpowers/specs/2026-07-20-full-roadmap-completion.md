# Spec — Full Roadmap + PRD + Expansion Completion Program

**Date:** 2026-07-20 (rev 2)  
**Status:** Authoritative plan set for finishing product **end-to-end**, including former PRD “out of scope” expansions and channel binaries  
**Index:** `docs/superpowers/plans/2026-07-20-INDEX.md`  
**Sources:** `docs/PRD.md`, `docs/ROADMAP.md`, `docs/ARCHITECTURE.md`, investigation 2026-07-19

---

## 1. Definition of “done”

| Layer | Done means |
|---|---|
| **PRD §6 MVP acceptance** | All 8 criteria pass via `ClaimsTriageAcceptanceTest` |
| **PRD §5 core features** | Including control checklist + full registry fields |
| **PRD §7 NFRs** | Isolation tests, retention, measurable hooks, sealed packs |
| **Roadmap Phase 0–5** | On `origin/main` including V10 |
| **Roadmap Phase 6** | SSO/OIDC, immutable audit, PDF pack, CI gate, observability, Docker/Terraform, isolation |
| **Phase 7 expansions** | Assisted determination, certification readiness, reg monitor, sector packs |
| **Go-to-market** | Landing legal/demo/SEO, GitHub baseline, metrics canon |
| **Channel assets** | CV DOCX/PDF, pitch deck, portfolio site match canon |

### Forever disclaimers (product may assist; must never claim)

- Not a law firm; not legal advice  
- Not a notified body; does **not** issue EU AI Act certificates  
- Not an official government regulatory bulletin  

These stay on landing, determination UI, readiness UI, and exports.

---

## 2. Goals

| ID | Goal | Part |
|---|---|---|
| G0–G12 | Phase 5 WIP → Phase 6 → docs (as rev 1) | 0–11 |
| G13 | Assisted obligation determination engine | **12** |
| G14 | Certification readiness automation | **13** |
| G15 | Regulatory change monitoring feed | **14** |
| G16 | Sector packs (insurance, HR, finance) + SPI | **15** |
| G17 | CV / pitch / portfolio binary alignment | **16** |

---

## 3. Plan index

| Part | File | Depends on |
|---|---|---|
| 0–4 | Prior channel + OAuth pack | see INDEX |
| 5 | PRD MVP | 0 |
| 6 | Immutable audit | 0 |
| 7 | Evidence pack PDF/seal | 5, 6 preferred |
| 8 | CI release-gate + observability | 1, 5 |
| 9 | Docker + Terraform | 0 |
| 10 | Isolation + NFR | 0 |
| 11 | Docs alignment | continuous |
| **12** | Obligation determination (assisted) | 5, 7 |
| **13** | Certification readiness | 5–7, 12 |
| **14** | Reg change monitoring | 5, 12 |
| **15** | Sector integrations | 5, 12 |
| **16** | Channel binaries | 2 final freeze |

---

## 4. Waves

```text
A: 0 → 1
B: 5 ∥ 10 ∥ 6
C: 7 ∥ 8 ∥ 9
D: 4
E: 12 → 13 → (14 ∥ 15)
F: 3 → 2 freeze → 11
G: 16
```

---

## 5. Why expansions use careful naming

Original PRD excluded full legal determination, automated certification, live reg monitoring, and sector integrations from **MVP** to avoid overclaim and scope explosion. They are **now in the program** as Phase 7, with product-safe definitions:

| Request | Deliverable name |
|---|---|
| Legal determination engine | Assisted obligation map + human review |
| Automated certification | Readiness score + gap report |
| Live reg monitoring | Polled feed + impact hints |
| Sector integrations | Three packs + connector SPI |

---

## 6. Success criteria (program)

- [ ] Parts 0–16 checklists complete  
- [ ] PRD updated: expansions listed as Phase 7 assisted features  
- [ ] ROADMAP Phase 6 + Phase 7 complete with evidence  
- [ ] No “certified / legal final determination” product claims  
- [ ] Channel assets signed off  
- [ ] CI green; live site + OAuth smoke; metrics canon frozen  

---

## 7. Related

- Index: `plans/2026-07-20-INDEX.md`  
- Investigation: `docs/investigations/2026-07-19-end-to-end-status-metrics-and-channel-alignment.md`  
- Channel instructions (draft): `docs/investigations/2026-07-19-channel-update-instructions.md`  
