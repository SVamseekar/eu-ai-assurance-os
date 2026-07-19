# Employment AI — human oversight SOP (sample)

> **Sample SOP only.** Sector pack: `hr`.

## Scope

Applies to AI systems used for recruitment, screening, shortlisting, or worker evaluation
registered under sector `hr` / `employment`.

## Roles

| Role | Responsibility |
|------|----------------|
| Hiring manager | Final decision; may override model rank |
| Recruiter | Reviews shortlist; documents overrides |
| Compliance | Periodic sampling of adverse outcomes |

## Mandatory human review

1. Any automatic **reject** recommendation before candidate notification
2. Any promotion / performance flag that triggers formal action
3. Batch re-ranks after model version changes

## Override logging

Record: actor, system id, candidate id (or pseudonym), prior score, new decision, rationale.

## Related controls

- `HR_HUMAN_OVERSIGHT_EMPLOYMENT`
- `HUMAN_OVERSIGHT` (baseline catalog)
