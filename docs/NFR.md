# Non-functional requirements — targets vs measurement

This document records **PRD §7** targets honestly: they are **product goals**, not certificates of production compliance until measured in a real environment.

## PRD §7 targets (goals)

| Requirement | Target | Status |
|---|---|---|
| p95 API latency for registry reads | &lt; 300 ms | **Target** — instrumented; not certified |
| p95 RAG answer latency | &lt; 4 s (MVP) | **Target** — instrumented; not certified |
| Uptime | 99.9% production | **Ops SLO** — not claimed from unit tests |
| Tenant data isolation | No cross-tenant leakage | **Enforced** — regression suite in CI |
| Encryption in transit / at rest | TLS + provider disk encryption | **Documented** — see below and `SECURITY.md` |
| Audit retention | Configurable ≥ 7 years | Implemented (Part 6); see audit config |
| Evidence pack deterministic / traceable | Export + audit event | Partial (JSON export); PDF seal is Part 7 |

## Latency measurement hooks

Micrometer timers (Spring Actuator / Prometheus scrape when enabled):

| Metric name | Tag | Covers |
|---|---|---|
| `assurance.api.registry.read` | `operation` = `list` \| `get` | `GET /api/v1/systems`, `GET /api/v1/systems/{id}` |
| `assurance.api.evidence.query` | `operation` = `answer` | `POST /api/v1/evidence/query` |

### How to query p95 (example)

With Actuator metrics exposed (`management.endpoints.web.exposure.include` includes `metrics` / Prometheus):

```bash
# Micrometer timer snapshot (exact property names depend on registry backend)
curl -s http://localhost:8080/actuator/metrics/assurance.api.registry.read \
  | jq '.measurements'

# Prefer Prometheus histogram quantiles in production:
# histogram_quantile(0.95, sum(rate(assurance_api_registry_read_seconds_bucket[5m])) by (le))
```

**Do not** treat green unit tests or a single local `curl` as p95 compliance. p95 requires production (or load-test) volume under realistic concurrency and payload sizes.

### Honest limits of local measurement

- H2 in-memory CI runs are not representative of Postgres + network latency.
- RAG latency includes embedding retrieval; `local-hash` provider is not production embedding cost/latency.
- Fake “always under 300 ms” assertions in tests would create **false SLO compliance** — we deliberately avoid them.

## Uptime 99.9% (ops SLO)

99.9% monthly availability ≈ ≤ 43 minutes downtime / month.

**Recommended monitoring approach (not auto-provisioned by this repo yet):**

1. **Synthetic checks** — UptimeRobot / Checkly / Vercel Monitoring against:
   - Dashboard origin (landing + `/login`)
   - API `GET /actuator/health` (and `/actuator/health/liveness`, `/readiness` when deployed)
2. **Platform status** — Vercel (dashboard) + host/DB provider (API/Postgres) incident feeds
3. **Actuator** — liveness/readiness for orchestrators (K8s, ECS, etc.) once Part 9 ships containers
4. **Alerting** — page on multi-region failure of health + login path, not on single-instance process restarts alone

Until production deploy + monitoring exist, **do not advertise 99.9% as measured**.

## Encryption and secrets (NFR)

### In transit

- **Browser → dashboard (Vercel / reverse proxy):** TLS terminated at the edge (Vercel HTTPS or load balancer).
- **Dashboard BFF → Spring API:** Prefer TLS in production (`ASSURANCE_API_BASE_URL=https://...`). Local dev may use `http://localhost:8080`.
- **API → Postgres / object storage:** Provider TLS for managed DBs and S3/GCS HTTPS endpoints.

### At rest

- **Postgres disk encryption** is the **cloud/provider responsibility** (e.g. RDS/Cloud SQL encryption-at-rest). Application code does not re-encrypt row payloads.
- **Evidence objects** in object storage should use bucket default encryption (SSE-S3 / SSE-KMS or GCS default).
- **API keys** are stored as SHA-256 hashes only (`api_keys.key_hash`), never raw.
- **Passwords** use BCrypt; JWT signing keys are DB-managed RSA material — protect DB access and backups.

### Secrets handling

| Secret | Source | Notes |
|---|---|---|
| `EVAL_CALLBACK_SECRET` | Env / secret manager | Required; empty fails closed outside permissive local config |
| `AUDIT_CHAIN_SECRET` | Env / secret manager | Hash-chain HMAC; set in production |
| `DATABASE_PASSWORD` | Env / secret manager | Never commit |
| OAuth client secrets (Part 4) | Env / secret manager | Not in git |

- **No secrets in the repository** — gitleaks (Part 1) and code review.
- Rotate eval callback and audit chain secrets via secret manager; redeploy workers/API together for callback secret changes.

## Tenant isolation (measured in CI)

Cross-tenant isolation is regression-tested (`TenantIsolationTest`): tenant A credentials never receive tenant B resource bodies (expect **404** or **403**, never **200** with B data). See also `docs/ROLE_MATRIX.md` and `docs/SECURITY.md`.
