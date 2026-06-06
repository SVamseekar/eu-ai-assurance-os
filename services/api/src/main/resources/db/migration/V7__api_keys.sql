create table api_keys (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  user_id uuid not null references users(id),
  created_at timestamp with time zone not null
);

create index idx_api_keys_tenant_id on api_keys (tenant_id);
