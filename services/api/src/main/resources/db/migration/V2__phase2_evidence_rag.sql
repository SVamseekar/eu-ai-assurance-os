create table evidence_documents (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  system_id uuid not null references ai_systems(id),
  type varchar(128) not null,
  title varchar(255) not null,
  source_uri varchar(2048) not null,
  checksum varchar(128) not null,
  chunk_count integer not null check (chunk_count >= 0),
  ingestion_status varchar(64) not null,
  created_at timestamp with time zone not null
);

create table evidence_chunks (
  id uuid primary key,
  document_id uuid not null references evidence_documents(id),
  ordinal integer not null check (ordinal >= 0),
  section_ref varchar(255),
  content varchar(8192) not null,
  embedding text not null,
  metadata_json text not null
);

create table evidence_queries (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  system_id uuid not null references ai_systems(id),
  question varchar(2048) not null,
  answer varchar(8192) not null,
  confidence double precision not null check (confidence >= 0 and confidence <= 1),
  citations_json text not null,
  created_by uuid not null references users(id),
  created_at timestamp with time zone not null
);

create index idx_evidence_documents_tenant_system
  on evidence_documents (tenant_id, system_id, created_at);

create index idx_evidence_chunks_document
  on evidence_chunks (document_id, ordinal);

create index idx_evidence_queries_tenant_system
  on evidence_queries (tenant_id, system_id, created_at);
