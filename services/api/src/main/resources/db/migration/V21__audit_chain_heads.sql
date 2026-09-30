create table audit_chain_heads (
  tenant_id uuid primary key references tenants(id),
  head_hash varchar(64),
  updated_at timestamp with time zone not null
);

-- One head per existing tenant. head_hash stays null until the next append, which then links to the
-- latest existing event (legacy chains keep their continuity).
insert into audit_chain_heads (tenant_id, head_hash, updated_at)
select t.id, null, current_timestamp from tenants t;

alter table audit_events add column source varchar(16) default 'system' not null;
