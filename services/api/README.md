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
- `POST /api/v1/eval-runs`
- `GET /api/v1/eval-runs/{id}`
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

Run against PostgreSQL:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/eu_ai_assurance \
DATABASE_USERNAME=eu_ai_assurance \
DATABASE_PASSWORD=eu_ai_assurance \
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

Run the PostgreSQL smoke test with a temporary local database:

```bash
bash scripts/postgres-smoke.sh
```
