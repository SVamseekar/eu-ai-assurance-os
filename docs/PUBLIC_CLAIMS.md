# Public-claims teasers

Four EU organisations reconstructed from **public pages only**, then run through Evgraph and the Assurance release gate.

They are **not customers**. The registry names them so a sales demo can say: *this is what a fail-closed check sees from what you already published*.

| Slug | Public hook | Assisted risk note |
|------|-------------|--------------------|
| `getsafe` | Press 5 Jun 2025: AI agents handle claims, advice, contract close | Annex III §5(b)-shaped (insurance / essential private services) |
| `auxmoney` | COO Dec 2025: ML credit-risk, >90% automation | Annex III §5(b)-shaped (creditworthiness) |
| `softgarden` | Product: AI Matching highlights applicants | Annex III §4-shaped (employment / recruitment) |
| `retorio` | Publishes an AI Act stance; also describes HR selection | Employment-shaped; **do not adjudicate Art. 5** |

Annex III high-risk duties apply **2 December 2027**. Article 50 has applied since **2 August 2026**. Assisted notes are not legal classifications.

## Evgraph (library)

Canonical files: `services/api/src/main/resources/public-claims/<slug>/`

```bash
./scripts/public-claims-evgraph.sh
```

Each pack **must** exit non-zero:

- `scan-promotion --gate --strict` — public pages do not publish `approved_at` (INCONCLUSIVE → fail)
- `scan-dataset-manifest --gate` — public pages do not publish a dataset license (EXPECTATION_NOT_MET)

Do not invent an approval timestamp to force a prettier fail. Absence is the finding.

## API

Requires a session. Local H2 seeds the four systems when `assurance.demo.public-claims=true` (default on H2, **false** on postgres).

- `GET /api/v1/public-claims`
- `GET /api/v1/public-claims/{slug}`
- `GET /api/v1/public-claims/{slug}/evgraph`
- Dashboard: `/public-claims` (authenticated)
- Public marketing: `/method` (sourced teasers, not customers)

Paying tenants: keep `ASSURANCE_PUBLIC_CLAIMS=false`. Ops readiness treats postgres + seeding as a blocker.

## What this is not

A certificate, a cold-email attack, a scrape of private data, or a claim that these firms are non-compliant.
