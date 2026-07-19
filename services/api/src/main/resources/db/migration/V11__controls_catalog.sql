create table controls (
  id uuid primary key,
  code varchar(64) not null unique,
  name varchar(255) not null,
  description varchar(2048) not null,
  applies_to_risk_class varchar(128) not null,
  category varchar(64) not null
);

create table system_controls (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  system_id uuid not null references ai_systems(id),
  control_id uuid not null references controls(id),
  status varchar(32) not null check (status in ('PASS', 'REVIEW', 'BLOCKED')),
  evidence_required boolean not null default true,
  reviewer_id uuid references users(id),
  notes varchar(2048),
  updated_at timestamp with time zone not null,
  unique (tenant_id, system_id, control_id)
);

create index idx_system_controls_tenant_system
  on system_controls(tenant_id, system_id);

create index idx_system_controls_tenant_status
  on system_controls(tenant_id, status);
