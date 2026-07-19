# Contributing to EU AI Assurance OS

Thanks for helping improve the governance control plane for EU AI Act–oriented release assurance.

## Git workflow

1. **Branch from `main`** with a descriptive name:
   - `feature/…` — new capability
   - `fix/…` — bug fix
   - `chore/…` — tooling, CI, docs-only
2. **Keep commits focused** — one logical change per commit when practical.
3. **Open a pull request** into `main`. Prefer PR review even for solo work so CI runs on the branch.
4. **Wait for CI** — `API tests`, `Dashboard checks`, and `Secret scan` should pass before merge.
5. **Do not force-push to `main`.** Feature branches may be rebased if they have not been shared widely.

## Local setup

### API (Spring Boot 3.3 / Java 17)

```bash
cd services/api
mvn test
mvn spring-boot:run
# → http://localhost:8080
```

Default H2 profile needs no external database. `EVAL_CALLBACK_SECRET` may be empty for local H2; set a real secret in any shared environment.

### Dashboard (Next.js 16)

```bash
cd apps/dashboard
npm ci
npx tsc --noEmit
npm run build
npm run dev
# → http://localhost:3000
```

The dashboard proxies authenticated API traffic through `/api/proxy` to `ASSURANCE_API_BASE_URL` (default `http://localhost:8080`). When the API is unreachable, many views fall back to seeded mock data.

## Required checks before opening a PR

| Area | Command |
|---|---|
| API unit/integration tests | `cd services/api && mvn test` |
| Dashboard types | `cd apps/dashboard && npx tsc --noEmit` |
| Dashboard production build | `cd apps/dashboard && npm run build` |

## Never commit

- `.env`, `.env.*`, API keys, JWT signing material, or production credentials
- Tenant customer data or real personal data used as fixtures
- Large binary dumps unrelated to the product (prefer Git LFS only if deliberately adopted)

## Branch protection (maintainers)

`main` should require:

- A pull request (0 approving reviews is OK for solo admin)
- Status checks: **API tests**, **Dashboard checks**, **Secret scan**
- No force-push / no branch deletion

If the GitHub plan or permissions block rulesets, document the gap and re-apply when available. Solo admins may retain bypass for emergency hotfixes — prefer a follow-up PR the same day.

## Repo settings (maintainers)

- Homepage URL: `https://euassuranceai.souravamseekar.com`
- License: MIT
- Topics: keep AI Act / Spring Boot / Next.js topics accurate

## Security reports

See [SECURITY.md](./SECURITY.md). Do not open public issues for undisclosed vulnerabilities.
