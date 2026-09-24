alter table mapping_proposals
  add column control_mode varchar(32);

alter table mapping_proposals
  add column reopened_at timestamp with time zone;

create table system_change_scope (
  system_id uuid primary key references ai_systems(id),
  tenant_id uuid not null references tenants(id),
  prompt varchar(4000),
  retrieval_corpus varchar(4000),
  retention varchar(4000),
  human_review_logic varchar(4000)
);
