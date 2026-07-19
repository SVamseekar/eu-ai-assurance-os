# Claims model card starter (sample)

> **Sample template only.** Not a certified model card. Sector pack: `insurance`.

## Model details

| Field | Value |
|-------|-------|
| Model name | {{model_name}} |
| Version | {{model_version}} |
| Vendor | {{vendor_name}} |
| Intended use | Claims triage / routing **assist** (not final deny without human review) |
| Out of scope | Fully automated final claim denial without review |

## Training data

- Sources: {{data_sources}}
- Known gaps / under-represented segments: _______________

## Performance

| Metric | In-sample | Holdout | Notes |
|--------|-----------|---------|-------|
| Routing accuracy | | | |
| False adverse rate | | | |
| Latency p95 | | | |

## Fairness slices

Document protected-attribute and segment fairness results; link eval run IDs.

## Human oversight points

1. Pre-adverse decision review
2. Escalation path for edge claims
3. Model version pin on release

## Limitations and ethical considerations

List known failure modes (e.g. rare claim types, multi-language narratives).

## Contact

Model owner: {{owner}}
