create table approval_workflows (
  id          uuid primary key,
  tenant_id   uuid not null references tenants(id),
  system_id   uuid not null references ai_systems(id),
  trigger     varchar(64) not null check (trigger in (
                'SYSTEM_CREATED', 'EVAL_REGRESSION', 'CONTRACT_BREACH',
                'RISK_RECLASSIFIED', 'HIGH_RISK_PASS')),
  status      varchar(32) not null check (status in (
                'OPEN', 'APPROVED', 'REJECTED', 'SUPERSEDED')),
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
