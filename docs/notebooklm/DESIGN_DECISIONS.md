# DESIGN DECISIONS

This document records key architectural and domain design decisions in an Architecture Decision Record (ADR) format: **Context → Decision → Consequences**. Every decision is grounded in empirical codebase evidence, git commit history, and security audits.

---

## 1. Tenant Identity Must Originate Exclusively from Verified Credentials

- **Context**: Multi-tenant SaaS applications must isolate tenant data cleanly. In an early implementation phase (identified during internal security audit `docs/SECURITY_AUDIT_2026-06-22.md`), `TenantContextFilter` fell back to trusting raw `X-Tenant-Id` and `X-Actor-Id` headers when no API key was present, validating only that the IDs existed in the database rather than verifying authorization. The frontend compounded this vulnerability by reading tenant/actor values from `localStorage` (defaulting to demo IDs `tenant-premium` / `actor-priya`) and attaching them as HTTP headers. This allowed any user to open browser Developer Tools, modify `localStorage`, and spoof requests into other tenant accounts.
- **Decision**: Tenant and actor identities are resolved exclusively from validated JWT claims (via signed cookies/tokens) or verified API keys (`ak_live_*`). Client-supplied `X-Tenant-Id` / `X-Actor-Id` headers are strictly ignored for authorization. On the frontend, `apps/dashboard` was converted to a Next.js BFF pattern: browser sessions hold `httpOnly` secure cookies, all API calls pass through a server-side proxy (`/api/proxy`) that injects verified Bearer tokens, and `middleware.ts` enforces session validation on all app routes.
- **Consequences**: Eliminates cross-tenant data impersonation vectors. Supported by automated integration tests (`TenantIsolationTest`) asserting that Tenant A receives a `404 Not Found` or `403 Forbidden` when requesting Tenant B resources. The tradeoff is architectural: the dashboard cannot operate as a static SPA and requires a server runtime (e.g., Vercel / Node.js) to host the BFF layer.

---

## 2. Cryptographic Hash-Chaining for Audit Log Integrity

- **Context**: Standard database audit logs consisting of simple `INSERT`-only tables do not satisfy external regulatory auditors. An auditor's primary concern is tamper-evidence: proving that historical log records were not modified, deleted, or inserted out of sequence by a database administrator or via compromised database credentials.
- **Decision**: Implemented `AuditChainHasher`, which generates an HMAC-SHA-256 signature for every audit event. The hash is calculated over a canonical, sorted-key string representation of the event fields (`tenantId|id|prevHash|actorId|eventType|resourceType|resourceId|payloadJson|createdAt`) combined with the preceding event's hash (`prev_event_hash`). Each row stores `prev_event_hash`, `event_hash`, and a `retain_until` timestamp (defaulting to 7 years). Two verification REST endpoints (`GET /audit/verify` and `GET /audit-events/verify-chain`) traverse the chain and validate integrity.
- **Consequences**: Direct database modifications outside the application break the cryptographic chain from the point of alteration forward, making tampering immediately detectable. The tradeoff is secret management dependency: the hash chain relies on `assurance.audit.chain-secret` (or environment variable `AUDIT_CHAIN_SECRET`). If the secret is compromised, an attacker with direct DB write access could recompute a valid chain.

---

## 3. Two-Pass Engineering Iteration for Outbound SSRF Defense

- **Context**: The evidence pipeline allows users to register remote document URIs (`sourceUri`) for automated text extraction. Because outbound HTTP calls originate from inside the server trust boundary, this creates Server-Side Request Forgery (SSRF) and DNS-rebinding risks — where a hostname resolves to a public IP during validation but re-resolves to a private IP (e.g. `127.0.0.1`, `169.254.169.254`) when the HTTP connection opens.
- **Iteration 1 Fix (`c323ea9`)**: Resolved the IP address before opening a connection and rejected loopback, link-local, site-local, or multicast IP ranges.
- **Flaw Discovered**: JDK's default `HttpClient` re-resolves hostnames independently when establishing sockets, leaving a Time-of-Check to Time-of-Use (TOCTOU) race window. An attempted fix rewrote the URI to the target IP while attempting to set a custom `Host` header, but JDK `HttpClient` rejects `Host` as a restricted header — causing all remote evidence fetches to silently fail and fall back to metadata stubs.
- **Iteration 2 Decision (`ed0ecd5`)**: Replaced JDK `HttpClient` with Apache HttpClient5 and registered a custom single-lookup `DnsResolver` on `PoolingHttpClientConnectionManager`. The custom resolver resolves the hostname exactly once, validates the IP against blacklisted private ranges, and supplies that exact validated IP to the connection socket.
- **Consequences**: Completely closes the DNS-rebinding SSRF vulnerability while preserving outbound HTTPS evidence extraction functionality. Required adding an explicit Maven dependency management override for `org.apache.httpcomponents.core5:httpcore5` to prevent Spring Boot's parent BOM from downgrading the transport library.

---

## 4. Constant-Time Password Verification to Prevent Account Enumeration

- **Context**: Authentication endpoints that perform database user lookups before executing password hashing introduce a timing oracle. Requests for non-existent email addresses return instantly, whereas requests for valid email addresses take ~100ms due to bcrypt execution. Attackers can exploit this timing difference to enumerate valid user accounts.
- **Decision (`bea2582`)**: Standardized `AuthService.login()` to execute bcrypt comparison on every login attempt. If a user email does not exist in the database, the service executes bcrypt against a pre-computed dummy password hash.
- **Consequences**: Equalizes execution latency across valid and invalid user login requests, preventing timing-based account enumeration. (The same commit corrected a refresh token revocation bug where a token's forward-pointer was recursively set to itself rather than cleared).

---

## 5. Release Gate Fusion of Four Compliance Signals into CI Exit Codes

- **Context**: Compliance evaluation involves multiple distinct domain metrics: evidence coverage, eval benchmark scores, data contract drift status, control evaluation states, and human oversight logs. Evaluating these signals independently allows risky releases to slip through (e.g., a system with a 95% eval score shipping despite an open data contract breach).
- **Decision**: `ReleaseGateService.calculate()` unifies all four signals into a single deterministic evaluation:
  - **`BLOCKED`**: Triggered if risk class is `PROHIBITED`, any control is `BLOCKED`, data contract status is `BREACH`, eval score is below hard threshold (`78`), or a `HIGH` risk system lacks mandatory legal sign-off oversight evidence.
  - **`REVIEW`**: Triggered if evidence coverage is below pass threshold (`82%`), eval score is below soft threshold (`85`), data contract is `WARNING`, or any open gaps exist.
  - **`PASS`**: Triggered when all thresholds and controls are satisfied.
  - **Machine Interfaces**: Exposes `GET /ci/release-gate`, returning JSON along with process exit code semantics (`PASS` = 0, `BLOCKED` = 1, `REVIEW` = 2) for direct consumption by GitHub Actions or Jenkins CI/CD pipelines.
- **Consequences**: Deployment pipelines evaluate compliance identical to human dashboard reviews. Current limitation: threshold values (`EVAL_PASS_THRESHOLD = 85`, `EVAL_HARD_BLOCK_THRESHOLD = 78`, `EVIDENCE_PASS_THRESHOLD = 82`) are hardcoded class constants. Per-tenant or per-sector threshold overrides are not currently supported.

---

## 6. Durable DB-Backed Polling Queue for Eval Execution

- **Context**: Asynchronous processing of evaluation dataset runs requires queue management, retry handling, and concurrent worker locking. Standard implementations often introduce external messaging middleware like Kafka, RabbitMQ, or AWS SQS.
- **Decision**: Implemented `EvalRunQueueWorker`, a Spring scheduled service polling `eval_runs` every 5000ms (`assurance.eval.worker.poll-interval-ms`). Workers acquire pending jobs using `SELECT ... FOR UPDATE SKIP LOCKED`, allowing multiple API replicas to process queued evaluation runs without duplicate execution. State transitions and retry metadata (`worker_attempts`, `max_attempts`, `failure_reason`, `started_at`, `completed_at`) are stored directly on the `eval_runs` table.
- **Consequences**: Zero additional stateful infrastructure dependencies. The queue is fully covered by existing PostgreSQL transaction management, backup routines, and tenant isolation filtering. The trade-off is polling latency (5-second floor) and database write load during high queue volumes.

---

## 7. Pluggable Dual-Embedding Provider Seam

- **Context**: Retrieval-Augmented Generation (RAG) over compliance evidence requires vector embeddings. However, requiring ONNX/PyTorch model downloads and CPU/GPU inference during local development and CI test suites introduces flakiness, slowness, and external network dependencies.
- **Decision**: Created the `EvidenceEmbeddingProvider` interface with two implementations:
  - **`local-hash`** (Active in default/test profiles): Generates deterministic, hash-based pseudo-embeddings in-memory without model weights or network access. Used for fast H2 unit and integration testing.
  - **`djl-sentence`** (Active in `postgres` profile): Loads `sentence-transformers/all-MiniLM-L6-v2` via DJL and ONNX Runtime to generate 384-dimensional dense semantic vectors, storing them in PostgreSQL with pgvector HNSW indexing (`V4` Flyway migration).
- **Consequences**: Unit test suites execute in seconds offline, while production environments utilize semantic vector search. Requires maintaining provider dimension compatibility when upgrading embedding models.

---

## 8. Structural Disclaimers Built into Assistive Phase 7 Models

- **Context**: Features such as obligation determination, readiness scoring, and regulatory change monitoring could expose users to legal liability if marketed or structured as automated legal certification.
- **Decision**: Structural boundaries are baked into data models and API response types:
  - **Obligation Determination**: API responses explicitly return `autoApplied: false` and `requiresHumanConfirm: true`. The engine suggests applicable EU AI Act articles but never automatically alters an AI system's recorded risk classification.
  - **Certification Readiness Score**: Computes a weighted 0–100 score across 9 governance dimensions, but the API response schema intentionally omits any `certified` boolean field.
  - **Regulatory Change Monitoring**: Regulatory update feeds default item impact hints to `UNCERTAIN` and explicitly include `autoMutatesRiskOrControls: false` in audit payloads.
- **Consequences**: Programmatically prevents the software from asserting legal compliance or issuing statutory CE certificates. Aligns with the project's canonical "do not claim" specification (`docs/METRICS_CANONICAL.md`).

---

## 9. Next.js BFF Security & Session Mediation

- **Context**: SPA client applications sending raw authentication tokens directly to backend APIs risk token exfiltration via XSS or browser extension sniffing.
- **Decision**: Implemented Next.js 16 Backend-for-Frontend (BFF) patterns (`apps/dashboard`):
  - Session tokens are stored in `httpOnly`, `SameSite=Lax`, `Secure` cookies (`session_access`, `session_refresh`).
  - Next.js server proxy (`/api/proxy`) intercepts outgoing requests, injects Bearer JWT headers server-side, forwards requests to Spring Boot, and handles token refresh flow transparently.
  - Next.js `middleware.ts` validates authenticated sessions on all protected app-shell routes (`/command`, `/systems`, `/approvals`, etc.).
- **Consequences**: Prevents client-side JavaScript access to authentication credentials and enforces strict session boundary validation.

---

## 10. Negative Design Findings (Explicitly Not Implemented)

To maintain absolute accuracy, the following patterns were evaluated and verified **absent** from the codebase:
- **No Blockchain or Distributed Ledger**: Audit integrity relies on database-level HMAC-SHA-256 hash chains, not distributed ledgers or blockchain consensus.
- **No Dual-Write Architecture**: Synchronous multi-database writing patterns are not utilized.
- **No LLM-as-a-Judge Evaluation**: Eval metrics in the queue worker are computed deterministically (or supplied via signed external callback), not via LLM self-evaluation.
- **No Live Third-Party Vendor Integrations**: Sector pack integration connectors (`IntegrationConnector`) ship as `LoggingIntegrationConnector` stubs without live OAuth or API links to third-party HR/Insurance vendors.
