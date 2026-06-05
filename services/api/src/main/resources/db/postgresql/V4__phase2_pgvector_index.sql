do $$
begin
  if exists (select 1 from pg_available_extensions where name = 'vector') then
    create extension if not exists vector;

    alter table evidence_chunks
      add column if not exists embedding_vector vector(64);

    create index if not exists idx_evidence_chunks_embedding_hnsw
      on evidence_chunks using hnsw (embedding_vector vector_cosine_ops);
  else
    raise notice 'pgvector extension is not available; skipping evidence embedding vector index';
  end if;
end $$;
