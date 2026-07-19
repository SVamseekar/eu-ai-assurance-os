# Security Policy

## Supported versions

| Version / branch | Supported |
|---|---|
| `main` (latest) | Yes |
| Older tags / forks | Best effort only |

## Reporting a vulnerability

Please **do not** open a public GitHub issue for security vulnerabilities.

Email a private report to **souravamseekar@gmail.com** (or the address listed on the repository owner profile) with:

- A description of the issue and impact
- Steps to reproduce or a proof-of-concept
- Affected commit SHA or release tag if known

You should receive an acknowledgement within a few business days when possible.

## Product security documentation

Threat model, controls, and audit notes live in-repo:

- [docs/SECURITY.md](./docs/SECURITY.md) — threat model and security controls
- [docs/SECURITY_AUDIT_2026-06-22.md](./docs/SECURITY_AUDIT_2026-06-22.md) — audit remediation snapshot

## Scope notes

- This product is a **governance control plane**, not a notified-body conformity assessment tool.
- Authentication today is password JWT + API keys; OAuth/OIDC is on the roadmap.
- Eval result callbacks use HMAC-SHA-256 signatures; treat `EVAL_CALLBACK_SECRET` as production-sensitive.
