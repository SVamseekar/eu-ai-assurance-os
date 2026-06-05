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
recalculates the system release gate. Completing a run that is already
`completed` returns `409 Conflict`.

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
GET /systems/{systemId}/data-contracts
POST /data-contracts/{contractId}/drift-events
```

Request:

```json
{
  "severity": "breach",
  "field": "denial_reason_category",
  "description": "New field is not mapped to fairness monitoring."
}
```

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

## Evidence Pack

```http
GET /systems/{systemId}/evidence-pack
```

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
  "auditEvents": []
}
```

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
