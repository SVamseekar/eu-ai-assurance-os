# Spec — Channel Parity, Product Completeness & Industry Ops

**Date:** 2026-07-19  
**Status:** Subsumed into full program — use **`docs/superpowers/specs/2026-07-20-full-roadmap-completion.md`** + **`plans/2026-07-20-INDEX.md`** for complete scope. This document remains valid for Parts **0–4** only.  
**Source investigation:** `docs/investigations/2026-07-19-end-to-end-status-metrics-and-channel-alignment.md`

## Problem

1. Local Phase 5 completion is **uncommitted** while ROADMAP locally claims Phase 5 complete.  
2. Public GitHub hygiene is below sister products (no CI, license, branch protection).  
3. Marketing claims on CV / Portfolio / Pitch / Landing **diverge from the codebase** (FAISS, HMAC audit stream, Next 14, V1–V6, automated classification, live-since-2024).  
4. Landing lacks enterprise surface: demo form, legal/refunds, complete footer.  
5. OAuth/OIDC not implemented; only password JWT + API keys.  
6. SEO is good baseline but incomplete once legal/demo pages exist.

## Goals

| ID | Goal |
|---|---|
| G0 | Land Phase 5 WIP on `main` via PR with green tests |
| G1 | Industry-grade GitHub ops matching WorkforceGuard baseline |
| G2 | Single metrics canon; all channels use the same numbers/wording |
| G3 | Landing: demo form + legal (privacy/terms/refunds) + footer parity |
| G4 | SEO hardened for all public pages |
| G5 | OAuth E2E (Google + Microsoft) following WorkforceGuard patterns |
| G6 | Non-code channels updated via **instruction packets** only in other sessions |

## Non-goals

- Editing PDF/DOCX/PPT in this repo session.  
- Full enterprise IdP marketplace (Okta/SAML) in first OAuth slice — start with Google + Microsoft.  
- Claiming legal certification or EU AI Act conformity assessment.  
- Hash-chained immutable audit storage (now **Part 6** of full program, not deferred indefinitely).

## Success criteria

- [ ] Clean `main`; Phase 5 notifications + V10 migrated; approvals UI uses live notification APIs  
- [ ] CI green on every PR; `main` protected  
- [ ] `docs/METRICS_CANONICAL.md` exists and matches measured code  
- [ ] Live `/privacy`, `/terms`, `/refunds`, `/request-demo` return 200  
- [ ] Demo submission reaches Discord webhook with rate limit + honeypot  
- [ ] Footer has Product / Resources / Legal / Contact  
- [ ] Sitemap lists all public pages; robots still disallows app shell  
- [ ] OAuth login works locally and on production with smoke checklist  
- [ ] CV / Portfolio / Pitch instruction packet applied in separate sessions; claims match canon  

## Architecture decisions

1. **Auth:** Keep JWT + refresh + API keys; add OAuth as alternate IdP that issues the same session cookie shape.  
2. **Demo form:** Vercel route handler under `apps/dashboard/app/api/request-demo/route.ts` (Next App Router equivalent of WorkforceGuard’s `api/request-demo.js`).  
3. **Legal pages:** Next.js App Router routes with shared layout chrome; enterprise refunds copy (order form / SOW), not consumer pro-rata.  
4. **Metrics:** One markdown canon; optional small `lib/metrics-canon.ts` for landing strip.  
5. **Git:** Feature branches + PR; no direct push to main after protection.

## Plan partition

| Part | Name | Depends on |
|---|---|---|
| 0 | Finish Phase 5 WIP | — |
| 1 | Industry-grade GitHub | 0 preferred |
| 2 | Metrics canon + channel instruction packet | 0 (numbers freeze) |
| 3 | Landing legal + demo + SEO + footer | 2 for metrics strip |
| 4 | OAuth E2E | 0; can parallel 3 after auth baseline |

## Risks

| Risk | Mitigation |
|---|---|
| Overclaiming before V10 merges | Part 0 stop-gate |
| Discord webhook missing in prod | Fail closed with support email fallback |
| OAuth misconfig redirects | Smoke test doc like WorkforceGuard |
| Solo-dev branch protection friction | Allow admin bypass; require CI status only |

## Out of scope files for Claude Chat sessions

- CV DOCX/PDF  
- Pitch PDF  
- Portfolio content (unless a code session is opened on Portfolio repo)  

Those receive **copy instructions** from the investigation report §8 and Part 2 plan.
