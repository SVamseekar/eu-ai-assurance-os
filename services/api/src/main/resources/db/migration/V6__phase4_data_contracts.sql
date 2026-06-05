create table data_contracts (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  system_id uuid not null references ai_systems(id),
  name varchar(255) not null,
  owner varchar(255) not null,
  version varchar(255) not null,
  status varchar(32) not null check (status in ('HEALTHY', 'WARNING', 'BREACH')),
  coverage integer not null check (coverage >= 0 and coverage <= 100),
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null,
  unique (tenant_id, system_id, name, version)
);

create index idx_data_contracts_tenant_system on data_contracts(tenant_id, system_id);

create table drift_events (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  contract_id uuid not null references data_contracts(id),
  severity varchar(32) not null check (severity in ('INFO', 'WARNING', 'BREACH')),
  field varchar(255),
  description varchar(2048) not null,
  status varchar(32) not null check (status in ('OPEN', 'ACKNOWLEDGED', 'RESOLVED')),
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);

create index idx_drift_events_tenant_contract on drift_events(tenant_id, contract_id);
create index idx_drift_events_tenant_contract_status on drift_events(tenant_id, contract_id, status);
