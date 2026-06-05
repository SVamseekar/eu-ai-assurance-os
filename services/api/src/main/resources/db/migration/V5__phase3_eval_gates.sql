create table eval_datasets (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  name varchar(255) not null,
  version varchar(128) not null,
  sample_count integer not null check (sample_count > 0),
  golden boolean not null,
  created_at timestamp with time zone not null,
  unique (tenant_id, name, version)
);

alter table eval_runs
  add column dataset_id uuid references eval_datasets(id);

alter table eval_runs
  add column queued_at timestamp with time zone;

alter table eval_runs
  add column started_at timestamp with time zone;

alter table eval_runs
  add column completed_at timestamp with time zone;

alter table eval_runs
  add column failed_at timestamp with time zone;

alter table eval_runs
  add column worker_attempts integer not null default 0 check (worker_attempts >= 0);

alter table eval_runs
  add column max_attempts integer not null default 3 check (max_attempts > 0);

alter table eval_runs
  add column failure_reason varchar(2048);

update eval_runs
  set queued_at = created_at
  where queued_at is null;

alter table eval_runs
  alter column queued_at set not null;

create index idx_eval_datasets_tenant_name
  on eval_datasets (tenant_id, name, version);

create index idx_eval_runs_tenant_system
  on eval_runs (tenant_id, system_id, created_at);

create index idx_eval_runs_dispatch
  on eval_runs (status, queued_at);
