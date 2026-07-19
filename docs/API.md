# API Contract

Base path:

```text
/api/v1
```

MVP tenant context headers:

```http
X-Tenant-Id: 00000000-0000-0000-0000-000000000001
X-Actor-Id: 00000000-0000-0000-0000-000000000101
```

If omitted, the backend uses the bootstrapped MVP tenant and actor. If provided,
both headers must refer to known records.

## AI Systems

```http
POST /systems
```

Request:


```json
{
  "name": "Claims Triage AI",
  "owner": "Insurance Ops",
  "purpose": "Prioritize and route insurance claims",
  "riskClass": "high",
  "riskBasis": "Access to essential private services",
  "deploymentRegion": "EU"
}
```

Response:

```json
{
  "id": "sys_001",
  "releaseDecision": "blocked",
  "createdAt": "2026-06-05T10:00:00Z"
}
```

```http
GET /systems
GET /systems/{systemId}
PATCH /systems/{systemId}
```

## Risk Classification

```http
POST /systems/{systemId}/risk-classification
```

Request:

```json
{
  "riskClass": "high",
  "basis": "Supports access decisions for essential services",
  "affectedUsers": ["claimants", "reviewers"],
  "humanOversightRequired": true
}
```

## Evidence

```http
POST /evidence/documents
```

Request:

```json
{
  "systemId": "sys_001",
  "type": "DPIA",
  "title": "Claims Triage DPIA",
  "sourceUri": "s3://tenant-a/dpia/claims.pdf",
  "content": "Optional extracted text for local indexing.",
  "checksum": "optional-upstream-checksum",
  "metadata": {
    "version": "2026-06"
  }
}
```

Response:

```json
{
  "id": "doc_123",
  "systemId": "sys_001",
  "type": "DPIA",
  "title": "Claims Triage DPIA",
  "sourceUri": "s3://tenant-a/dpia/claims.pdf",
  "checksum": "sha256-or-upstream-checksum",
  "chunkCount": 4,
  "ingestionStatus": "indexed",
  "createdAt": "2026-06-05T10:05:00Z"
}
```

The `content` field is optional and backward compatible with metadata-only
uploads. When provided, the API validates allowed source URI schemes, checksum
shape, content length, metadata shape, and chunk size; strips
prompt-injection-like document lines; stores chunk SHA-256 hashes and embedding
provider provenance; embeds each chunk; and stores the index for cited
retrieval. Metadata-only uploads are indexed with a generated placeholder text
until object-store extraction is wired in.

```http
GET /evidence/systems/{systemId}/documents
```

```http
POST /evidence/query
```

Request:

```json
{
  "systemId": "sys_001",
  "question": "Which controls block this release?"
}
```

Response:

```json
{
  "answer": "The release is blocked because human oversight evidence and bias evaluation are incomplete.",
  "confidence": 0.82,
  "citations": [
    {
      "documentId": "doc_123",
      "title": "Claims Triage DPIA",
      "section": "Human oversight",
      "snippet": "Reviewer override must include purpose, affected cohort, appeal route, and owner sign-off."
    }
  ]
}
```

## Eval Gates

```http
POST /eval-datasets
```

Request:

```json
{
  "name": "golden-eu-claims-v4",
  "version": "2026-06",
  "sampleCount": 240,
  "golden": true
}
```

Response:

```json
{
  "id": "dataset_001",
  "name": "golden-eu-claims-v4",
  "version": "2026-06",
  "sampleCount": 240,
  "golden": true,
  "createdAt": "2026-06-05T10:00:00Z"
}
```

```http
GET /eval-datasets
```

```http
POST /eval-runs
```

The `dataset` value must reference a registered eval dataset name. When multiple
versions share a name, new runs use the latest registered version. The MVP
bootstrap registers `golden-eu-claims-v4`. Created runs enter a durable
`queued` state and are eligible for the background eval worker.

Request:

```json
{
  "systemId": "sys_001",
  "dataset": "golden-eu-claims-v4",
  "modelVersion": "claims-triage-2026-06-05",
  "promptVersion": "claims-routing-v12",
  "threshold": 0.85
}
```

Response:

```json
{
  "runId": "eval_001",
  "status": "queued"
}
```

```http
PATCH /eval-runs/{runId}/result
```

Required headers:

```http
X-Eval-Timestamp: 1780689600
X-Eval-Signature: v1=<hex hmac sha256>
```

Request:

```json
{
  "metrics": {
    "faithfulness": 0.78,
    "relevance": 0.76,
    "safetyRefusal": 0.91,
    "biasSlicePassRate": 0.74,
    "latencyP95Ms": 1800,
    "costUsd": 4.62
  }
}
```

The result callback requires numeric `faithfulness`, `relevance`,
`safetyRefusal`, and `biasSlicePassRate` metrics. It marks the run `completed`,
stores the metrics, derives the system eval score from scored metrics, and
recalculates the system release gate. The signature is computed over
`<timestamp>.<raw request body>` using `assurance.eval.callback.secret`.
Callbacks with identical metrics are idempotent; conflicting replays return
`409 Conflict`.

```http
POST /eval-runs/{runId}/execute
```

Manually executes a queued eval run immediately with the same backend-owned
worker path used by background dispatch. The durable worker records
`running`, increments `workerAttempts`, generates deterministic MVP metrics for
faithfulness, relevance, safety refusal, bias slice pass rate, latency, and
cost, completes the run, stores metrics, and recalculates the system release
gate. Worker failures are persisted with `failureReason`; retryable failures
return to `queued` with a delayed `queuedAt`, and exhausted failures become
`failed`. Re-executing a completed run returns `409 Conflict`.

```http
GET /eval-runs/operations
```

Returns tenant-scoped operational visibility for queued, running, retryable,
and failed eval runs.

```http
POST /eval-runs/{runId}/retry
```

Requeues a failed eval run, clears terminal failure metadata, resets
`workerAttempts` to zero, and appends an audit event.

```http
GET /eval-runs/{runId}
```

Response:

```json
{
  "runId": "eval_001",
  "systemId": "sys_001",
  "datasetId": "dataset_001",
  "status": "completed",
  "dataset": "golden-eu-claims-v4",
  "modelVersion": "claims-triage-2026-06-05",
  "promptVersion": "claims-routing-v12",
  "threshold": 0.85,
  "metrics": {
    "faithfulness": 0.78,
    "relevance": 0.76,
    "biasSlicePassRate": 0.74,
    "safetyRefusal": 0.91,
    "latencyP95Ms": 1800,
    "costUsd": 4.62
  },
  "releaseDecision": "BLOCKED",
  "createdAt": "2026-06-05T10:00:00Z",
  "queuedAt": "2026-06-05T10:00:00Z",
  "startedAt": "2026-06-05T10:00:03Z",
  "completedAt": "2026-06-05T10:00:04Z",
  "failedAt": null,
  "workerAttempts": 1,
  "maxAttempts": 3,
  "failureReason": null
}
```

## Data Contracts

```http
POST /data-contracts
GET /data-contracts
GET /data-contracts?systemId={systemId}
GET /data-contracts/{contractId}
PATCH /data-contracts/{contractId}
GET /data-contracts/{contractId}/drift-events
POST /data-contracts/{contractId}/drift-events
PATCH /data-contracts/{contractId}/drift-events/{eventId}
```

Create contract request:

```json
{
  "systemId": "sys_001",
  "name": "Claims Input Schema",
  "owner": "Data Platform",
  "version": "2026-06",
  "status": "healthy",
  "coverage": 96
}
```

Drift event request:

```json
{
  "severity": "breach",
  "field": "denial_reason_category",
  "description": "New field is not mapped to fairness monitoring."
}
```

Open `breach` drift marks the contract and mapped system as `BREACH`, causing
the release gate to block. Resolving the event with:

```json
{
  "status": "resolved"
}
```

recalculates the contract, system data-contract status, and release decision.

## Audit chain verification

```http
GET /audit/verify
GET /audit-events/verify-chain
```

Returns `{ "valid": true, "checkedCount": N, "firstBreakId": null }` for the
tenant’s HMAC-SHA-256 hash-chained audit ledger. Events store `prevEventHash`,
`eventHash`, and `retainUntil` (≥ 7 years by default). There is no public API to
update or delete audit rows.

## Controls catalog

```http
GET /controls
GET /systems/{systemId}/controls
PUT /systems/{systemId}/controls/{controlId}
```

Catalog controls are seeded with EU AI Act–style obligations (risk management,
data governance, human oversight, transparency, etc.). Creating or
reclassifying a system attaches applicable controls. `PUT` body:

```json
{
  "status": "PASS",
  "notes": "Oversight SOP v3 reviewed"
}
```

Statuses: `PASS` | `REVIEW` | `BLOCKED`. Any `BLOCKED` system control appears in
release-gate blockers as `CONTROL:{code}`.

Registry fields on AI systems (create/update/risk classification):
`vendorName`, `modelName`, `modelVersion`, `dataSources`, `sector`,
`decisionImpact`, `affectedUsers`.

## Assisted obligation determination (Part 12)

**Product framing:** responses are a **suggested applicability / obligation map** only.
They are **not legal advice**, not a final legal determination, not certification, and
not an official conformity assessment. Human legal review is always required.
Risk class is never auto-changed.

```http
GET /determination/questionnaire
POST /systems/{systemId}/determination/runs
GET /systems/{systemId}/determination/runs
GET /systems/{systemId}/determination/runs/{runId}
```

`GET /determination/questionnaire` returns versioned questions, `rulesetVersion`,
`productLabel` (`Assisted obligation determination (ruleset vX)`), and a full disclaimer.

`POST /systems/{systemId}/determination/runs` body:

```json
{
  "answers": {
    "sector": "insurance",
    "decision_impact": "eligibility",
    "essential_private_service": true,
    "biometric": false,
    "employment": false,
    "human_in_loop": true,
    "interacts_with_natural_persons": true,
    "profiling": true,
    "users_affected": "many",
    "high_risk_self_assessment": true
  }
}
```

Response includes `disclaimer`, `rulesetVersion`, evaluated obligations with
`applicability` (`APPLICABLE` | `NOT_APPLICABLE` | `UNCERTAIN`), mapped `controlCodes`,
and `result.riskSuggestion` with `autoApplied: false` / `requiresHumanConfirm: true`.
Applicable control codes open missing `system_controls` in `REVIEW`. Audit event:
`determination.run.completed`. Latest run is embedded in evidence pack JSON/PDF under
`determination` (with the same disclaimer).

## Certification readiness (Part 13)

**Product framing:** responses are a **readiness score + structured gap report** only.
They are **not legal certification**, not notified-body attestation, and not an
official conformity assessment. Never returns a `certified: true` field.

```http
GET  /systems/{systemId}/certification-readiness
POST /systems/{systemId}/certification-readiness/export
```

`GET` returns:

| Field | Description |
|---|---|
| `score` | Weighted readiness 0–100 |
| `readinessStatus` | `NOT_READY` \| `READY_FOR_REVIEW` \| `GAPS` |
| `productLabel` | `Certification readiness automation` |
| `disclaimer` | Full product-safe disclaimer |
| `dimensions[]` | Nine dimensions: risk, controls, evidence, eval, contracts, approvals, oversight, determination, audit_chain |
| `gaps[]` | `{ code, severity, message, remediationHint, dimension }` |

Weights and thresholds are configurable via `assurance.certification-readiness.*`
(defaults sum to 100: risk 10, controls 15, evidence 15, eval 15, contracts 10,
approvals 10, oversight 10, determination 10, audit chain 5).

`POST .../export` body `{ "format": "json" | "pdf" }` (default `json`) returns a
downloadable readiness report. PDF is a human-readable export; JSON is primary.
Audit events: `certification_readiness.assessed`, `certification_readiness.exported`.

## Approval Workflows

```http
GET /workflows/open
GET /workflows/mine
GET /workflow-notifications/mine
GET /systems/{systemId}/workflows
GET /systems/{systemId}/workflows/active
```

Workflow cycles are opened automatically when system creation, risk
reclassification, eval regression, or data-contract breach requires review.
Stages are assigned to tenant users by required role and must be completed in
stage order. ADMIN actors may override a stage with rationale.

```http
POST /systems/{systemId}/workflows/{workflowId}/stages/{stageId}/approve
POST /systems/{systemId}/workflows/{workflowId}/stages/{stageId}/reject
POST /systems/{systemId}/workflows/{workflowId}/stages/{stageId}/override
```

Approve request:

```json
{
  "rationale": "Evidence reviewed and accepted.",
  "oversightEvidence": "Human oversight SOP v3, section 4.2"
}
```

`oversightEvidence` is required for legal signoff approval. Rejection and
override requests require `rationale`. Stage actions append audit events and
create notifications for the next assigned reviewer.

## Release Gate

```http
GET /systems/{systemId}/release-gate
```

Response:

```json
{
  "systemId": "sys_001",
  "decision": "blocked",
  "blockers": [
    "Human oversight SOP missing",
    "Bias eval below threshold",
    "claims_events.v4 data contract breach"
  ]
}
```

### CI/CD machine contract (Part 8)

Preferred for deploy bots (same decision engine; richer payload):

```http
GET /ci/release-gate?systemId={uuid}
```

Auth: `X-Api-Key` (recommended for CI service accounts) or Bearer JWT.

Response:

```json
{
  "systemId": "…",
  "systemName": "Claims Triage AI",
  "decision": "BLOCKED",
  "blockers": ["Data contract breach is open"],
  "evalScore": 78,
  "evidenceCoverage": 72,
  "dataContractStatus": "BREACH",
  "riskClass": "HIGH",
  "exitCode": 1,
  "content": "Release gate BLOCKED — do not deploy. Blockers: …"
}
```

CLI helper: `scripts/ci-release-gate.sh` — exit **PASS=0**, **BLOCKED=1**, **REVIEW=2**. See `docs/OPS.md` and `.github/workflows/release-gate-example.yml`.

## Evidence Pack

JSON is the **primary** sealed, machine-readable export (PRD MVP). PDF is a Phase 6
human-readable export of the same sealed content.

### JSON (primary)

```http
GET /systems/{systemId}/evidence-pack
```

Roles: `ADMIN`, `AI_ENGINEERING_LEAD`, `COMPLIANCE_OFFICER`, `LEGAL_COUNSEL`, `AUDITOR`.

Response:

```json
{
  "systemId": "sys_001",
  "generatedAt": "2026-06-05T10:30:00Z",
  "decision": "blocked",
  "riskClassification": {},
  "evidence": [],
  "evalRuns": [],
  "dataContracts": [],
  "approvals": [],
  "auditEvents": [],
  "evidencePackVersion": "1.0",
  "contentSha256": "hex-sha256-of-canonical-json",
  "generator": "eu-ai-assurance-api/0.1.0",
  "auditChainHead": "hex-hmac-sha256-of-latest-audit-event-or-null"
}
```

Seal notes:

- `evidencePackVersion` is currently `"1.0"`.
- `contentSha256` is SHA-256 over canonical JSON (sorted map keys, ISO-8601 instants)
  of the pack fields **excluding** `contentSha256` itself.
- `generator` identifies the service build (`assurance.evidence-pack.generator`).
- `auditChainHead` is the tenant audit hash-chain tip **before** this export event (Part 6).
- Export writes audit event `evidence_pack.exported` with payload
  `{ decision, contentSha256, format, evidencePackVersion }` (`format` is `json` or `pdf`).
- Same system state + same clock → same `contentSha256` (clock is injectable for tests).

### PDF (Phase 6 polish)

```http
GET /systems/{systemId}/evidence-pack.pdf
```

Same roles as JSON. Returns `Content-Type: application/pdf` with:

- `Content-Disposition: attachment; filename="evidence-pack-{systemId}-{yyyy-MM-dd}.pdf"`
- `X-Content-Sha256: <same seal as JSON pack for this export>`

PDF sections: system identity, risk summary, evidence/evals/contracts/approvals, audit
excerpt, seal footer. Prefer the JSON pack for cryptographic verification of the seal.

## Audit

```http
GET /audit-events?systemId=sys_001
```

```http
POST /audit-events
```

Request:

```json
{
  "systemId": "sys_001",
  "eventType": "approval.override_requested",
  "resourceType": "approval",
  "resourceId": "approval_001",
  "payload": {
    "reason": "Manual compliance review required"
  }
}
```

Audit events are append-only and cannot be modified through public APIs.
