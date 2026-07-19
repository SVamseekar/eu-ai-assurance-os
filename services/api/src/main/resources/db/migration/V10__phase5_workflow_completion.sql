alter table approval_stages
  add column assigned_reviewer_id uuid references users(id);

alter table approval_stages
  add column oversight_evidence varchar(2048);

alter table approval_stages
  add column notification_sent_at timestamp with time zone;

create table workflow_notifications (
  id            uuid primary key,
  tenant_id     uuid not null references tenants(id),
  workflow_id   uuid not null references approval_workflows(id),
  stage_id      uuid references approval_stages(id),
  recipient_id  uuid references users(id),
  event_type    varchar(64) not null,
  message       varchar(512) not null,
  read_at       timestamp with time zone,
  created_at    timestamp with time zone not null
);

create index idx_workflow_notifications_tenant_recipient
  on workflow_notifications(tenant_id, recipient_id, read_at, created_at);

create index idx_workflow_notifications_tenant_workflow
  on workflow_notifications(tenant_id, workflow_id, created_at);
