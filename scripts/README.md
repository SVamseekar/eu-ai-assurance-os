# Scripts

## `ci-release-gate.sh` (Part 8)

Call Assurance OS from a deploy pipeline and fail when the system is BLOCKED.

```bash
ASSURANCE_API_BASE=http://localhost:8080 \
API_KEY=00000000-0000-0000-0000-000000000a01 \
SYSTEM_ID=<uuid> \
./scripts/ci-release-gate.sh
```

| Exit | Meaning |
|---|---|
| 0 | PASS |
| 1 | BLOCKED (or REVIEW when `FAIL_ON_REVIEW=1`) |
| 2 | REVIEW |
| 3 | Missing env / usage |
| 4 | HTTP / transport / parse error |

Requires `curl` and either `jq` or `python3`. Full contract: `docs/OPS.md`.

## `postgres-ci.sh`

Local/CI helpers for Postgres-backed smoke (see `services/api/scripts` and README).
