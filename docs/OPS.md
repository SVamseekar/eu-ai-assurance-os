# Operations — CI release gate & observability

Part 8 surface for deploy pipelines and operators. This is **not** a full Grafana stack; it is the product metrics + CI contract shipped with the API.

## CI/CD release gate

### Endpoint

```http
GET /api/v1/ci/release-gate?systemId={uuid}
Authorization: (prefer X-Api-Key for bots)
```

Stable JSON contract:

| Field | Type | Notes |
|---|---|---|
| `systemId` | UUID | Requested system |
| `systemName` | string | Display name |
| `decision` | `PASS` \| `REVIEW` \| `BLOCKED` | Same engine as UI gate |
| `blockers` | string[] | Empty when PASS |
| `evalScore` | int | Snapshot from registry |
| `evidenceCoverage` | int | Snapshot from registry |
| `dataContractStatus` | enum | HEALTHY / WARNING / BREACH |
| `riskClass` | enum | |
| `exitCode` | 0 \| 1 \| 2 | CLI convenience |
| `content` | string | One-line summary for logs |

**CLI exit mapping** (`scripts/ci-release-gate.sh`):

| Decision | Exit |
|---|---|
| PASS | **0** |
| BLOCKED | **1** |
| REVIEW | **2** |
| Missing env | 3 |
| HTTP / parse / transport | 4 |

Optional `FAIL_ON_REVIEW=1` maps REVIEW → exit 1 for strict deploys.

### API key for CI bots (recommended)

1. Create a service-account user in the tenant (role with release-gate read — any role per `ROLE_MATRIX.md`).
2. Insert an `api_keys` row with `key_hash = SHA-256(raw_key)` bound to that user/tenant (admin tooling / SQL; production should use a secret manager for the raw key).
3. In CI secrets store:
   - `ASSURANCE_API_BASE` — API origin, no trailing slash required
   - `ASSURANCE_API_KEY` / `API_KEY` — raw key
   - `SYSTEM_ID` — system under gate
4. Never use interactive user passwords or long-lived JWTs in CI when an API key is available.
5. Example workflow: `.github/workflows/release-gate-example.yml` (`workflow_call` / `workflow_dispatch`).

```bash
ASSURANCE_API_BASE=https://api.example.com \
API_KEY="$ASSURANCE_API_KEY" \
SYSTEM_ID="$SYSTEM_ID" \
./scripts/ci-release-gate.sh
```

Local H2 bootstrap seeds a **dev-only** key (`00000000-0000-0000-0000-000000000a01`) for the default compliance user — not for production.

Equivalent product UI path: `GET /api/v1/systems/{id}/release-gate` (same decision engine; CI endpoint adds machine fields).

## Actuator & Prometheus

| Endpoint | Auth | Purpose |
|---|---|---|
| `/actuator/health` (+ liveness/readiness) | Public | Probes |
| `/actuator/info` | API key / JWT | Build info |
| `/actuator/metrics` | API key / JWT | Micrometer snapshot |
| `/actuator/prometheus` | API key / JWT | Prometheus text scrape |

`management.endpoints.web.exposure.include` defaults to `health,info,metrics,prometheus`.

**Do not** expose authenticated metrics publicly on the internet without network policy or mTLS. Prefer private scrape networks and short-lived scrape credentials.

### Product metrics (Part 8)

| Metric | Type | Tags | Meaning |
|---|---|---|---|
| `assurance.release_gate.decision` | counter | `decision` | Gate calculations served (UI + CI) |
| `assurance.api.evidence.query` | timer | `operation=answer` | RAG answer latency (Part 10; plan “evidence query latency”) |
| `assurance.api.registry.read` | timer | `operation=list\|get` | Registry read latency |
| `assurance.audit.append` | counter | — | Immutable audit appends |
| `assurance.auth.login.failures` | counter | `reason=invalid_credentials` | Failed password logins (no user enumeration tag) |
| `assurance.eval.run.*` | counter | various | Eval worker (existing) |

### Correlation IDs

`CorrelationFilter` sets MDC `requestId` from `X-Request-Id` (or generates one) and echoes the header. Non-default profiles use JSON logging with `requestId` via Logstash encoder (`logback-spring.xml`).

## Reading p95 (honest NFR)

See `docs/NFR.md`. Example Prometheus:

```promql
histogram_quantile(0.95,
  sum(rate(assurance_api_registry_read_seconds_bucket[5m])) by (le))
```

Unit tests only assert that **timers/counters exist after traffic** — they never claim p95 &lt; 300 ms.

## Local Compose stack (Part 9)

```bash
docker compose -f infra/docker-compose.yml --env-file .env.example up --build
```

- Health: `GET /actuator/health` (and liveness/readiness under `/actuator/health/*`)
- Secrets template: root `.env.example` (`EVAL_CALLBACK_SECRET`, `AUDIT_CHAIN_SECRET`, DB, storage)
- Deploy / rollback / Vercel matrix: `docs/DEPLOYMENT.md`
- Terraform skeleton validate (no cloud creds): `./scripts/terraform-validate.sh`

## Dashboard ops surface

`/command` summarizes:

- Release decision counts (blocked / review)
- Open contract breaches
- Open approval workflows (ops widget)
- Recent audit activity

API connectivity pill remains on Evidence (and similar) using existing health/proxy checks — **no secrets** in client health payloads.
