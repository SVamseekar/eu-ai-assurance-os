# Claims AI — DPIA starter (sample)

> **Sample template only.** Not legal advice. Adapt with your DPO / legal counsel.
> Sector pack: Insurance / Claims (`insurance`).

## 1. System summary

- **System name:** {{system_name}}
- **Purpose:** Claims triage / eligibility / routing automation
- **Owner:** {{owner}}
- **Deployment region:** {{deployment_region}}

## 2. Processing description

Describe personal data categories processed (claimants, policyholders, third parties),
lawful basis, and whether special-category data may appear in claim narratives.

## 3. Necessity and proportionality

Why automated triage is needed; what human process it augments (not replaces).

## 4. Risk to data subjects

| Risk | Likelihood | Severity | Mitigation |
|------|------------|----------|------------|
| Unfair denial / down-routing | | | Fairness testing (`INS_CLAIMS_FAIRNESS`) |
| Lack of meaningful review | | | Adverse decision human review (`INS_ADVERSE_DECISION_REVIEW`) |
| Opaque scores | | | Explainability for handlers (`INS_CLAIMS_EXPLAINABILITY`) |

## 5. Human oversight

Document stop/override paths for adverse automated outcomes and who is authorized.

## 6. Retention and logging

Align with operational logging controls and complaint-handling timelines.

## 7. Residual risk and sign-off

- Residual risk accepted by: _______________
- Date: _______________
