# API Service

Spring Boot backend MVP for the EU AI Assurance OS control plane.

The service uses Spring Data JPA repositories with Flyway-managed schema
migrations. The default profile runs against an in-memory H2 database for local
development and tests, while the `postgres` profile targets PostgreSQL using
environment-provided connection settings. A default MVP tenant and user are
bootstrapped until request-scoped auth is wired in.

Requests may pass `X-Tenant-Id` and `X-Actor-Id` headers. The API validates
provided header values against known tenant/user records and falls back to the
bootstrapped MVP tenant and actor when the headers are omitted.

Recommended initial stack:

- Java 21 for production; this MVP targets Java 17 so it builds on the current
  local toolchain.
- Spring Boot 3
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Testcontainers

First endpoints to implement:

- `POST /api/v1/systems`
- `GET /api/v1/systems`
- `GET /api/v1/systems/{id}/release-gate`
- `POST /api/v1/evidence/query`
- `POST /api/v1/eval-runs`
- `POST /api/v1/data-contracts/{id}/drift-events`
- `GET /api/v1/audit-events`
- `POST /api/v1/audit-events`

Implemented MVP endpoints:

- `POST /api/v1/systems`
- `GET /api/v1/systems`
- `GET /api/v1/systems/{id}`
- `PATCH /api/v1/systems/{id}`
- `POST /api/v1/systems/{id}/risk-classification`
- `GET /api/v1/systems/{id}/release-gate`
- `GET /api/v1/systems/{id}/evidence-pack`
- `POST /api/v1/evidence/documents`
- `GET /api/v1/evidence/systems/{id}/documents`
- `POST /api/v1/evidence/query`
- `POST /api/v1/eval-datasets`
- `GET /api/v1/eval-datasets`
- `POST /api/v1/eval-runs`
- `GET /api/v1/eval-runs/{id}`
- `PATCH /api/v1/eval-runs/{id}/result`
- `POST /api/v1/eval-runs/{id}/execute`
- `GET /api/v1/eval-runs/operations`
- `POST /api/v1/eval-runs/{id}/retry`
- `POST /api/v1/data-contracts/{id}/drift-events`
- `GET /api/v1/audit-events`
- `POST /api/v1/audit-events`

Run tests:

```bash
mvn test
```

Run the API:

```bash
mvn spring-boot:run
```

The API starts on:

```text
http://localhost:8080
```

The service allows local browser clients from common development ports,
including `localhost:8000`, `localhost:4173`, and Vite defaults, for the
static web prototype's evidence upload/query workflow.

Evidence/RAG hardening knobs:

- `assurance.evidence.embedding-provider`
- `assurance.evidence.max-content-characters`
- `assurance.evidence.max-metadata-entries`
- `assurance.evidence.max-metadata-value-characters`
- `assurance.evidence.max-question-characters`
- `assurance.evidence.max-retrieved-chunks`
- `assurance.evidence.min-retrieval-score`
- `assurance.evidence.allowed-source-schemes`

Eval worker knobs:

- `assurance.eval.worker.enabled`
- `assurance.eval.worker.poll-interval-ms`
- `assurance.eval.callback.secret`
- `assurance.eval.callback.signature-tolerance-seconds`

`EVAL_CALLBACK_SECRET` must be set in every runnable environment. Callback
requests to `PATCH /api/v1/eval-runs/{id}/result` must include
`X-Eval-Timestamp` and `X-Eval-Signature`, where the signature is
`v1=<hex hmac sha256>` over `<timestamp>.<raw request body>`.

Operational endpoints:

- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`

Eval metrics include:

- `assurance.eval.run.queued`
- `assurance.eval.run.claimed`
- `assurance.eval.run.completed`
- `assurance.eval.run.failed`
- `assurance.eval.run.retried`
- `assurance.eval.callback.signature.rejected`

The default profile uses portable text embeddings for H2 validation. The
`postgres` profile also loads `classpath:db/postgresql`, including the pgvector
HNSW index migration. If the `vector` extension is not installed on the
PostgreSQL server, the migration logs a notice and skips the optional vector
index while preserving the text embedding storage path.

Run against PostgreSQL:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/eu_ai_assurance \
DATABASE_USERNAME=eu_ai_assurance \
DATABASE_PASSWORD=eu_ai_assurance \
EVAL_CALLBACK_SECRET=replace-with-secret-manager-value \
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

Run the PostgreSQL smoke test with a temporary local database:

```bash
bash scripts/postgres-smoke.sh
```

Run Postgres eval claim concurrency validation against a configured Postgres
profile database:

```bash
bash scripts/postgres-concurrency.sh
```

Or point the concurrency validation at an existing Postgres database:

```bash
RUN_POSTGRES_CONCURRENCY=true \
DATABASE_URL=jdbc:postgresql://localhost:5432/eu_ai_assurance \
DATABASE_USERNAME=eu_ai_assurance \
DATABASE_PASSWORD=eu_ai_assurance \
EVAL_CALLBACK_SECRET=test-eval-callback-secret \
mvn test -Dtest=PostgresEvalRunConcurrencyTest
```
