create table tenants (
  id uuid primary key,
  name varchar(255) not null,
  plan varchar(64) not null,
  data_region varchar(64) not null,
  created_at timestamp with time zone not null
);

create table users (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  email varchar(320) not null,
  role varchar(64) not null,
  created_at timestamp with time zone not null,
  unique (tenant_id, email)
);

create table ai_systems (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  name varchar(255) not null,
  owner varchar(255) not null,
  purpose varchar(2048) not null,
  risk_class varchar(32) not null check (risk_class in ('MINIMAL', 'LIMITED', 'HIGH', 'PROHIBITED')),
  risk_basis varchar(2048) not null,
  deployment_region varchar(64) not null,
  evidence_coverage integer not null check (evidence_coverage >= 0 and evidence_coverage <= 100),
  eval_score integer not null check (eval_score >= 0 and eval_score <= 100),
  data_contract_status varchar(32) not null check (data_contract_status in ('HEALTHY', 'WARNING', 'BREACH')),
  release_decision varchar(32) not null check (release_decision in ('PASS', 'REVIEW', 'BLOCKED')),
  open_gaps varchar(4096) not null,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);

create table eval_runs (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  system_id uuid not null references ai_systems(id),
  status varchar(32) not null check (status in ('queued', 'running', 'completed', 'failed')),
  dataset varchar(255) not null,
  model_version varchar(255) not null,
  prompt_version varchar(255) not null,
  threshold double precision not null check (threshold >= 0 and threshold <= 1),
  metrics_json text not null,
  release_decision varchar(32) not null check (release_decision in ('PASS', 'REVIEW', 'BLOCKED')),
  created_at timestamp with time zone not null
);

create table audit_events (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  system_id uuid references ai_systems(id),
  actor_id uuid not null references users(id),
  event_type varchar(128) not null,
  resource_type varchar(128) not null,
  resource_id varchar(255),
  payload_json text not null,
  created_at timestamp with time zone not null
);
