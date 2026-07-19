# EU AI Assurance OS

**Live dashboard / landing:** [https://euassuranceai.souravamseekar.com](https://euassuranceai.souravamseekar.com)

EU AI Assurance OS is a governance control plane for teams shipping AI systems in the European market. It validates AI releases against EU AI Act–oriented controls by combining:

- AI system registry and risk classification (`MINIMAL` / `LIMITED` / `HIGH` / `PROHIBITED`)
- Cited compliance evidence RAG (local-hash embeddings in dev; DJL/ONNX all-MiniLM-L6-v2 on postgres)
- Eval gates with durable worker queue and HMAC-signed result callbacks
- Data-contract drift monitoring
- Multi-stage approval workflows (reviewer assignment, human oversight evidence, in-app notifications)
- Audit-ready evidence packs and append-only audit events

> This product assists governance and release readiness. It is **not** a notified body, does **not** issue legal certifications, and does **not** replace qualified legal counsel.

## Stack

| Layer | Technology |
|---|---|
| Dashboard + landing | **Next.js 16**, React 19, TypeScript, Tailwind CSS v4, TanStack Query, shadcn/ui |
| API | **Spring Boot 3.3**, Java 17, Flyway, Spring Data JPA |
| Auth (today) | Password JWT + refresh tokens, API keys; **OAuth/OIDC not yet** |
| Data | H2 (local default) or PostgreSQL (+ optional pgvector) |
| Deploy | Dashboard on Vercel; API hosted separately (apply Flyway on the API DB) |

## Repository layout

```text
apps/dashboard/   Production Next.js app (landing + authenticated dashboard)
apps/web/         Legacy static HTML/CSS/JS prototype (reference only)
services/api/     Spring Boot API MVP
docs/             PRD, architecture, API contract, schema, roadmap, security
infra/            Docker / compose placeholders
scripts/          Local and CI helper scripts
```

## Quick start

### 1. API

```bash
cd services/api
mvn test
mvn spring-boot:run
# → http://localhost:8080  (H2, Flyway through V10)
```

### 2. Dashboard

```bash
cd apps/dashboard
npm ci
npm run dev
# → http://localhost:3000
```

Bootstrap local login uses seeded users such as `compliance@example.com` with the documented dev password in `BootstrapData` (local only).

### Legacy prototype (optional)

```bash
python3 -m http.server 4173 --directory apps/web
# → http://localhost:4173
```

## Environment variables

| Variable | Where | Purpose |
|---|---|---|
| `EVAL_CALLBACK_SECRET` | API | HMAC secret for eval result callbacks (required in non-local envs) |
| `DATABASE_URL` / `DATABASE_USERNAME` / `DATABASE_PASSWORD` | API | Postgres profile |
| `ASSURANCE_API_BASE_URL` | Dashboard | Upstream API base (default `http://localhost:8080`) |
| `NEXT_PUBLIC_SITE_URL` | Dashboard | Canonical site URL for SEO/metadata |
| `NEXT_PUBLIC_GA_MEASUREMENT_ID` | Dashboard | Optional GA4 measurement id |

## Roadmap status (honest)

| Phase | Status on `main` |
|---|---|
| 0–4 Core product (systems, evidence, evals, contracts) | Complete |
| 5 Approval workflows + notifications + oversight | Complete (V10) |
| 6 Enterprise (OAuth, immutable audit, PDF packs, Docker/TF, deeper tenant NFRs) | Partial / in progress |
| Landing legal pages, industry CI, metrics freeze | In progress via `docs/superpowers/plans/` |

See [docs/ROADMAP.md](./docs/ROADMAP.md) and [docs/superpowers/plans/2026-07-20-INDEX.md](./docs/superpowers/plans/2026-07-20-INDEX.md).

## Documentation

- [docs/PRD.md](./docs/PRD.md) — product requirements
- [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md) — system design
- [docs/API.md](./docs/API.md) — HTTP contract
- [docs/SCHEMA.md](./docs/SCHEMA.md) — SQL tables
- [docs/SECURITY.md](./docs/SECURITY.md) — threat model
- [SECURITY.md](./SECURITY.md) — vulnerability reporting
- [CONTRIBUTING.md](./CONTRIBUTING.md) — PR and branch conventions

## Contributing & CI

PRs to `main` run GitHub Actions CI:

1. **Secret scan** (Gitleaks)
2. **API tests** — Java 17, `mvn -B test` (H2; empty `EVAL_CALLBACK_SECRET`)
3. **Dashboard checks** — Node 22, `tsc --noEmit`, `npm run build`

Never commit secrets. Prefer feature branches and conventional commits.

## License

MIT — see [LICENSE](./LICENSE).
