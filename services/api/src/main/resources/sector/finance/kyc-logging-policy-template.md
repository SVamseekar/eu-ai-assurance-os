# KYC / fraud assistant — logging policy (sample)

> **Sample policy only.** Sector pack: Financial services / KYC (`finance`).

## Purpose

Define elevated logging intensity for KYC and fraud-assistant AI so post-market monitoring,
complaint handling, and model governance remain auditable.

## Events to log

| Event | Minimum fields |
|-------|----------------|
| Model invocation | system id, model version, request id, timestamp |
| Risk / fraud score | score band (not raw PII beyond need), rule ids |
| Human override | actor, before/after decision, rationale |
| Release decision change | PASS / REVIEW / BLOCKED, gate reasons |

## Retention

Align with financial crime and complaints retention schedules (jurisdiction-specific).

## Related controls

- `FIN_KYC_LOGGING`
- `RECORD_KEEPING` (baseline)
