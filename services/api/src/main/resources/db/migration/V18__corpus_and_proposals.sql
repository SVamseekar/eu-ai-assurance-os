create table corpus_versions (
  id uuid primary key,
  version_hash varchar(64) not null unique,
  built_at timestamp with time zone not null
);

create table instruments (
  id uuid primary key,
  corpus_version_id uuid not null references corpus_versions(id),
  seed_celex varchar(32) not null,
  consolidation_celex varchar(64),
  title varchar(512) not null,
  language varchar(8) not null,
  text_hash varchar(64) not null,
  consolidation_date date,
  application_from date
);

create index idx_instruments_corpus on instruments(corpus_version_id);

create table provisions (
  id uuid primary key,
  instrument_id uuid not null references instruments(id),
  provision_key varchar(160) not null,
  article varchar(32),
  paragraph varchar(32),
  point varchar(32),
  annex varchar(32),
  text_excerpt varchar(4000) not null,
  force_status varchar(16) not null,
  force_from date not null,
  scope_note varchar(512),
  unique (instrument_id, provision_key)
);

create index idx_provisions_instrument on provisions(instrument_id);
create index idx_provisions_annex on provisions(annex);

create table guidance_docs (
  id uuid primary key,
  corpus_version_id uuid not null references corpus_versions(id),
  source_key varchar(128) not null,
  title varchar(512) not null,
  authority_rank varchar(64) not null,
  text_hash varchar(64) not null,
  body varchar(4000) not null
);

create index idx_guidance_docs_corpus on guidance_docs(corpus_version_id);

create table corpus_relationships (
  id uuid primary key,
  corpus_version_id uuid not null references corpus_versions(id),
  relation varchar(32) not null,
  from_kind varchar(32) not null,
  from_key varchar(160) not null,
  to_kind varchar(32) not null,
  to_key varchar(160) not null
);

create index idx_corpus_relationships_corpus on corpus_relationships(corpus_version_id);

create table mapping_proposals (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  system_id uuid not null references ai_systems(id),
  status varchar(32) not null,
  relation varchar(32) not null,
  corpus_version varchar(64) not null,
  adapter_version varchar(64),
  provision_key varchar(160),
  excerpt varchar(4000),
  created_at timestamp with time zone not null,
  decided_at timestamp with time zone,
  decided_by uuid
);

create index idx_mapping_proposals_tenant_system
  on mapping_proposals(tenant_id, system_id, created_at);
