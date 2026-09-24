create table assessment_applicability (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  system_id uuid not null references ai_systems(id),
  proposal_id uuid not null references mapping_proposals(id),
  applicability varchar(32) not null,
  reviewer_id uuid references users(id),
  unique (tenant_id, system_id, proposal_id)
);

create table evidence_exceptions (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  system_id uuid not null references ai_systems(id),
  proposal_id uuid not null references mapping_proposals(id),
  rationale varchar(2048) not null,
  expires_on date not null,
  created_by uuid not null references users(id),
  created_at timestamp with time zone not null
);

create index idx_evidence_exceptions_proposal
  on evidence_exceptions(tenant_id, system_id, proposal_id, created_at);
