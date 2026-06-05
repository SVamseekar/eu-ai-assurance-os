create extension if not exists vector;

alter table evidence_chunks
  add column embedding_vector vector(64);

create index idx_evidence_chunks_embedding_hnsw
  on evidence_chunks using hnsw (embedding_vector vector_cosine_ops);
