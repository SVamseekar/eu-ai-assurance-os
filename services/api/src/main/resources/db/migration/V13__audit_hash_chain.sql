alter table audit_events
  add column prev_event_hash varchar(64);

alter table audit_events
  add column event_hash varchar(64);

alter table audit_events
  add column retain_until timestamp with time zone;

-- Existing rows (if any) get genesis-style hashes filled lazily; new appends always set hashes.
create index idx_audit_events_tenant_created
  on audit_events(tenant_id, created_at);

create index idx_audit_events_tenant_retain
  on audit_events(tenant_id, retain_until);
