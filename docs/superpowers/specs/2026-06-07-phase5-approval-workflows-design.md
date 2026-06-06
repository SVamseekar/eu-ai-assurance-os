# Phase 5: Approval Workflows — Design Spec

Date: 2026-06-07  
Status: Approved for implementation

---

## 1. Context and regulatory basis

This phase implements the approval workflow component listed in the roadmap under Phase 5. The design is grounded in EU AI Act obligations as they stand post the May 2026 omnibus agreement:

- **Article 9** (Risk Management): continuous, documented process throughout lifecycle — workflows re-trigger on substantial modification, not just at registration
- **Article 14** (Human Oversight): HIGH-risk systems must have natural persons with competence and authority on record; structural, not score-conditional — HIGH-risk PASS systems still require compliance + legal sign-off once per cycle
- **Article 17** (QMS): roles, responsibilities and authorities must be defined, assigned, communicated and documented; prEN 18286 operationalises this as named individual attribution per lifecycle stage
- **Article 43 / Conformity Assessment**: new assessment required on substantial modification — the trigger model mirrors this definition directly

The May 2026 omnibus pushed Annex III high-risk deadlines to December 2027, but this tool is a governance control plane targeting best practice, not minimum legal floor.

**Key regulatory conclusion:** the Act is outcomes-based, not process-prescriptive. It does not mandate a specific number of approval stages or sequence. The three-stage sequential design below reflects industry standard (ModelOp, Credo AI, DataRobot, Holistic AI) and satisfies Article 17's accountability framework requirement.

---

## 2. Scope

**In scope for Phase 5:**
- Approval workflow state machine (open/close/supersede cycles)
- Three stage types: Eng Lead Review, Compliance Review, Legal Sign-off
- Role-based routing with full individual attribution at action time
- Override capability for ADMIN with mandatory rationale
- Workflow triggers on substantial modification events
- Evidence pack integration (approval history field)
- `/approvals` inbox route in the dashboard
- System detail approval tab and gate table workflow column
- In-app workflow state only — no email/webhook notifications

**Out of scope (Phase 6):**
- Named pre-assignment (locking a stage to a specific user before they act)
- Email/webhook notifications
- External notified body integrations

---

## 3. Domain model and schema

### 3.1 New tables

```sql
create table approval_workflows (
  id          uuid primary key,
  tenant_id   uuid not null references tenants(id),
  system_id   uuid not null references ai_systems(id),
  trigger     varchar(64) not null check (trigger in (
                'SYSTEM_CREATED', 'EVAL_REGRESSION', 'CONTRACT_BREACH',
                'RISK_RECLASSIFIED', 'HIGH_RISK_PASS')),
  status      varchar(32) not null check (status in (
                'OPEN', 'APPROVED', 'REJECTED', 'OVERRIDDEN', 'SUPERSEDED')),
  opened_at   timestamp with time zone not null,
  closed_at   timestamp with time zone,
  created_at  timestamp with time zone not null
);

create index idx_approval_workflows_tenant_system
  on approval_workflows(tenant_id, system_id);
create index idx_approval_workflows_tenant_status
  on approval_workflows(tenant_id, status);

create table approval_stages (
  id            uuid primary key,
  tenant_id     uuid not null references tenants(id),
  workflow_id   uuid not null references approval_workflows(id),
  stage_order   integer not null check (stage_order in (1, 2, 3)),
  stage_type    varchar(64) not null check (stage_type in (
                  'ENG_LEAD_REVIEW', 'COMPLIANCE_REVIEW', 'LEGAL_SIGNOFF')),
  required_role varchar(64) not null,
  status        varchar(32) not null check (status in (
                  'PENDING', 'APPROVED', 'REJECTED', 'OVERRIDDEN', 'SKIPPED')),
  actor_id      uuid references users(id),
  rationale     varchar(2048),
  acted_at      timestamp with time zone,
  created_at    timestamp with time zone not null
);

create index idx_approval_stages_workflow
  on approval_stages(workflow_id);
create index idx_approval_stages_tenant_workflow
  on approval_stages(tenant_id, workflow_id);
```

### 3.2 Invariants

- At most one `OPEN` workflow per system at a time. Enforced in `ApprovalWorkflowService` before opening a new cycle.
- All required stage rows are created upfront when a workflow opens (PENDING or SKIPPED). No lazy stage creation.
- A stage becomes actionable only when all prior stages are APPROVED or OVERRIDDEN.
- SKIPPED stages are non-actionable and do not block progression.

### 3.3 Stage configuration per risk class and gate result

| Risk class      | Gate result    | Stage 1 (Eng Lead) | Stage 2 (Compliance) | Stage 3 (Legal) |
|-----------------|----------------|-------------------|---------------------|----------------|
| PROHIBITED      | any            | No workflow opened — unconditionally blocked |
| HIGH            | PASS           | SKIPPED           | PENDING             | PENDING        |
| HIGH            | REVIEW/BLOCKED | PENDING           | PENDING             | PENDING        |
| LIMITED/MINIMAL | PASS           | No workflow opened — auto-cleared |
| LIMITED/MINIMAL | REVIEW         | PENDING           | PENDING             | SKIPPED        |
| LIMITED/MINIMAL | BLOCKED        | PENDING           | PENDING             | PENDING        |

---

## 4. Workflow trigger logic

The `ApprovalWorkflowService.openCycle(systemId, trigger)` method is called from these integration points:

| Event | Caller | Trigger value |
|---|---|---|
| System created with REVIEW or BLOCKED gate | `AiSystemController.createSystem` | `SYSTEM_CREATED` |
| HIGH-risk system with PASS gate and no prior APPROVED or OPEN workflow exists | `AiSystemController` (`saveWithCalculatedDecision`) | `HIGH_RISK_PASS` |
| Eval run callback drops gate to REVIEW or BLOCKED | `EvalRunWorkerService` (post result) | `EVAL_REGRESSION` |
| New BREACH drift event on a linked contract | `DataContractService.createDriftEvent` | `CONTRACT_BREACH` |
| Risk reclassification changes risk class or adds oversight gap | `AiSystemController.classifyRisk` | `RISK_RECLASSIFIED` |

**Supersede logic:** if an OPEN workflow already exists when `openCycle` is called, it is closed with status `SUPERSEDED` and `closed_at = now()` before the new cycle opens. This preserves the prior cycle in the audit trail.

### 4.1 Workflow lifecycle transitions

```
openCycle()  →  OPEN
  stage approved (not final)  →  OPEN (next stage becomes active)
  stage approved (final)      →  APPROVED, closed_at = now()
  stage rejected              →  REJECTED, closed_at = now()
  stage overridden (not final)→  OPEN (next stage becomes active)
  stage overridden (final)    →  APPROVED, closed_at = now()
  new substantial mod arrives →  SUPERSEDED, closed_at = now() → new OPEN cycle
```

### 4.2 Audit events written per action

| Action | event_type |
|---|---|
| Workflow opened | `approval.workflow.opened` |
| Stage approved | `approval.stage.approved` |
| Stage rejected | `approval.stage.rejected` |
| Stage overridden | `approval.stage.overridden` |
| Workflow closed (approved) | `approval.workflow.approved` |
| Workflow closed (rejected) | `approval.workflow.rejected` |
| Workflow superseded | `approval.workflow.superseded` |

Payload on stage events: `workflowId`, `stageId`, `stageType`, `rationale`, `actorId`, `actorRole`.

---

## 5. API contract

All routes are under `/api/v1/systems/{systemId}/workflows`. Workflows are always accessed through their system — no standalone top-level `/workflows` route.

### 5.1 Endpoints

```
GET  /api/v1/systems/{systemId}/workflows
     → list all workflow cycles (most recent first), stages embedded
     → available to all roles

GET  /api/v1/systems/{systemId}/workflows/active
     → returns single OPEN workflow with stages, or 404 if none
     → available to all roles; dashboard polls this

POST /api/v1/systems/{systemId}/workflows/{workflowId}/stages/{stageId}/approve
     body: { "rationale": "string (optional)" }
     → actor role must match stage required_role, or ADMIN
     → 409 if stage not PENDING or not yet active

POST /api/v1/systems/{systemId}/workflows/{workflowId}/stages/{stageId}/reject
     body: { "rationale": "string (required)" }
     → actor role must match stage required_role, or ADMIN
     → 409 if stage not PENDING or not yet active
     → closes workflow as REJECTED

POST /api/v1/systems/{systemId}/workflows/{workflowId}/stages/{stageId}/override
     body: { "rationale": "string (required)" }
     → ADMIN only; 403 for all other roles
     → marks stage OVERRIDDEN, advances workflow
```

### 5.2 Authorization rules (enforced in service, not controller)

- Approve/reject: `actor.role == stage.required_role || actor.role == ADMIN`
- Override: `actor.role == ADMIN` only
- AUDITOR: GET only — 403 on all POST endpoints
- Acting on non-PENDING stage: 409 Conflict with message "Stage is not pending"
- Acting on stage where prior stage is still PENDING: 409 Conflict with message "Prior stage not yet complete"

### 5.3 Evidence pack change

`GET /api/v1/systems/{systemId}/evidence-pack` gains a new top-level field:

```json
"approvalHistory": [
  {
    "workflowId": "...",
    "trigger": "SYSTEM_CREATED",
    "status": "APPROVED",
    "openedAt": "...",
    "closedAt": "...",
    "stages": [
      {
        "stageType": "ENG_LEAD_REVIEW",
        "status": "APPROVED",
        "actorId": "...",
        "actorEmail": "...",
        "rationale": "...",
        "actedAt": "..."
      }
    ]
  }
]
```

---

## 6. Backend package structure

New domain package: `os.assurance.eu.api.workflow`

```
workflow/
  ApprovalWorkflow.java          (domain record)
  ApprovalStage.java             (domain record)
  ApprovalWorkflowEntity.java    (JPA entity)
  ApprovalStageEntity.java       (JPA entity)
  ApprovalWorkflowJpaRepository.java
  ApprovalStageJpaRepository.java
  ApprovalWorkflowRepository.java  (domain seam interface)
  ApprovalWorkflowService.java   (state machine + trigger logic)
  ApprovalWorkflowController.java
  WorkflowTrigger.java           (enum)
  WorkflowStatus.java            (enum)
  StageType.java                 (enum)
  StageStatus.java               (enum)
  StageActionRequest.java        (approve/reject/override request body)
```

Follows the exact same layer conventions as `contract/`, `eval/`, `evidence/`.

---

## 7. Frontend

### 7.1 New route: `/approvals`

Added to sidebar nav between Systems and Evals. Serves as the reviewer inbox.

Two sections:
- **Awaiting your action** — stages where the current actor's role matches `required_role`, filtered to PENDING and active. Each row shows system name, stage label, time open, and Approve / Reject buttons.
- **In progress — other stages** — OPEN workflows where the active stage requires a different role. Read-only, shows who needs to act.

### 7.2 System detail — Approval tab

The slide-over panel on `/systems` gains an "Approval" tab:
- Current workflow status badge (OPEN / APPROVED / REJECTED / none)
- Active stage: role required, time waiting, action buttons (Approve / Reject, Override for ADMIN)
- Approval timeline: all closed cycles, each expanded to stage-by-stage trail with actor email, rationale, timestamp
- Override entries render in amber with rationale inline — visually distinct from clean approvals

### 7.3 Approve / Reject / Override modal

Triggered from either the `/approvals` inbox or the system detail Approval tab.
- Rationale textarea: optional for approve, required for reject and override
- Submit disabled until required rationale is filled
- On success: invalidates the active workflow query and refreshes gate table

### 7.4 Gate table on `/systems`

New "Workflow" column appended to `ReleaseGateTable`:
- `—` — no active workflow
- `● OPEN · Stage 2/3` — OPEN with current stage position
- `✓ Approved` — most recent cycle APPROVED
- `✗ Rejected` — most recent cycle REJECTED

### 7.5 Mock data

`lib/mock-data.ts` seeded with:
- Claims Triage AI (HIGH, BLOCKED): OPEN workflow, stage 1 active (ENG_LEAD_REVIEW PENDING)
- Support RAG Copilot (LIMITED, REVIEW): one closed APPROVED workflow in history, stage trail fully populated

---

## 8. What re-triggers a workflow cycle (substantial modification definition)

These events constitute a substantial modification in this system's terms and re-open a new workflow cycle, superseding any prior open cycle:

1. Eval score drops below REVIEW threshold (gate changes from PASS → REVIEW or REVIEW → BLOCKED)
2. New BREACH drift event on a linked data contract
3. Risk reclassification (risk class change or new oversight gap added)
4. System re-registered after being archived (Phase 6)

A new model version or prompt version that does not move the gate result is not treated as a substantial modification for workflow purposes — the eval run result is the gate signal.

---

## 9. Out of scope / deferred

- Email and webhook notifications (Phase 6)
- Named reviewer pre-assignment (Phase 6)
- Deadline SLAs on stages (Phase 6)
- External notified body integrations
- Workflow templates configurable per tenant
