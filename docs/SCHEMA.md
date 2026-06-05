# Data Model

## Core Tables

```sql
create table tenants (
  id uuid primary key,
  name text not null,
  plan text not null,
  data_region text not null,
  created_at timestamptz not null default now()
);

create table users (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  email text not null,
  role text not null,
  created_at timestamptz not null default now(),
  unique (tenant_id, email)
);

create table ai_systems (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  name text not null,
  owner text not null,
  purpose text not null,
  risk_class text not null check (risk_class in ('minimal', 'limited', 'high', 'prohibited')),
  risk_basis text not null,
  deployment_region text not null,
  release_decision text not null check (release_decision in ('pass', 'review', 'blocked')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table controls (
  id uuid primary key,
  code text not null unique,
  name text not null,
  description text not null,
  applies_to_risk_class text not null,
  category text not null
);

create table system_controls (
  id uuid primary key,
  system_id uuid not null references ai_systems(id),
  control_id uuid not null references controls(id),
  status text not null check (status in ('pass', 'review', 'blocked')),
  evidence_required boolean not null default true,
  reviewer_id uuid references users(id),
  updated_at timestamptz not null default now()
);
```

## Evidence Tables

```sql
create table evidence_documents (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  system_id uuid not null references ai_systems(id),
  type text not null,
  title text not null,
  source_uri text not null,
  checksum text not null,
  chunk_count int not null,
  ingestion_status text not null,
  created_at timestamptz not null default now()
);

create table evidence_chunks (
  id uuid primary key,
  document_id uuid not null references evidence_documents(id),
  ordinal int not null,
  section_ref text,
  content text not null,
  content_sha256 text,
  embedding text not null,
  embedding_provider text not null default 'local-hash',
  metadata_json text not null default '{}'
);

create table evidence_queries (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  system_id uuid not null references ai_systems(id),
  question text not null,
  answer text not null,
  confidence double precision not null,
  citations_json text not null,
  created_by uuid not null references users(id),
  created_at timestamptz not null default now()
);
```

The portable schema stores deterministic local embeddings as text so the same
core migrations validate against H2 and PostgreSQL. The PostgreSQL profile adds
a `pgvector` migration path with `embedding_vector vector(64)` and an HNSW
cosine index for production deployments using the local-hash provider. A
non-local embedding provider should ship its own matching vector dimension
migration.

## Eval Tables

```sql
create table eval_datasets (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  name text not null,
  version text not null,
  sample_count int not null,
  is_golden boolean not null default false,
  created_at timestamptz not null default now()
);

create table eval_runs (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  system_id uuid not null references ai_systems(id),
  dataset_id uuid not null references eval_datasets(id),
  model_version text not null,
  prompt_version text not null,
  threshold numeric not null,
  metrics jsonb not null default '{}',
  status text not null check (status in ('queued', 'running', 'completed', 'failed')),
  release_decision text not null check (release_decision in ('pass', 'review', 'blocked')),
  created_at timestamptz not null default now()
);
```

## Data Contract Tables

```sql
create table data_contracts (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  system_id uuid not null references ai_systems(id),
  name text not null,
  owner text not null,
  version text not null,
  status text not null check (status in ('healthy', 'warning', 'breach')),
  coverage numeric not null,
  created_at timestamptz not null default now()
);

create table drift_events (
  id uuid primary key,
  contract_id uuid not null references data_contracts(id),
  severity text not null check (severity in ('info', 'warning', 'breach')),
  field text,
  description text not null,
  status text not null check (status in ('open', 'acknowledged', 'resolved')),
  created_at timestamptz not null default now()
);
```

## Workflow and Audit Tables

```sql
create table approvals (
  id uuid primary key,
  system_id uuid not null references ai_systems(id),
  approver_id uuid references users(id),
  stage text not null,
  status text not null check (status in ('pending', 'approved', 'rejected')),
  justification text,
  created_at timestamptz not null default now(),
  decided_at timestamptz
);

create table audit_events (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  system_id uuid references ai_systems(id),
  actor_id uuid references users(id),
  event_type text not null,
  resource_type text not null,
  resource_id text,
  payload jsonb not null default '{}',
  created_at timestamptz not null default now()
);
```
