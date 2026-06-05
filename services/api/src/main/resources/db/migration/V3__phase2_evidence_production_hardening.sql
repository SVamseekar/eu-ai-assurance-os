alter table evidence_documents
  add column classification varchar(64) not null default 'internal';

alter table evidence_documents
  add column retention_until timestamp with time zone;

alter table evidence_chunks
  add column content_sha256 varchar(64);

alter table evidence_chunks
  add column embedding_provider varchar(64) not null default 'local-hash';

create index idx_evidence_documents_retention
  on evidence_documents (tenant_id, retention_until);

create index idx_evidence_chunks_content_sha256
  on evidence_chunks (content_sha256);
