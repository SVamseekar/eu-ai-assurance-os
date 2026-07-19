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
  golden boolean not null default false,
  created_at timestamptz not null default now(),
  unique (tenant_id, name, version)
);

create table eval_runs (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  system_id uuid not null references ai_systems(id),
  dataset_id uuid references eval_datasets(id),
  dataset text not null,
  model_version text not null,
  prompt_version text not null,
  threshold numeric not null,
  metrics_json text not null default '{}',
  status text not null check (status in ('queued', 'running', 'completed', 'failed')),
  release_decision text not null check (release_decision in ('PASS', 'REVIEW', 'BLOCKED')),
  created_at timestamptz not null default now(),
  queued_at timestamptz not null default now(),
  started_at timestamptz,
  completed_at timestamptz,
  failed_at timestamptz,
  worker_attempts int not null default 0,
  max_attempts int not null default 3,
  failure_reason text
);
```

The first Phase 3 migration keeps the legacy `dataset` text on `eval_runs` for
API compatibility and adds nullable `dataset_id` for registry-backed runs. New
runs are created against a registered dataset name. `queued_at`,
`worker_attempts`, `max_attempts`, and failure timestamps make the eval worker
queue durable enough to resume background dispatch after process restarts.

## Data Contract Tables

```sql
create table data_contracts (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  system_id uuid not null references ai_systems(id),
  name text not null,
  owner text not null,
  version text not null,
  status text not null check (status in ('HEALTHY', 'WARNING', 'BREACH')),
  coverage int not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (tenant_id, system_id, name, version)
);

create table drift_events (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  contract_id uuid not null references data_contracts(id),
  severity text not null check (severity in ('INFO', 'WARNING', 'BREACH')),
  field text,
  description text not null,
  status text not null check (status in ('OPEN', 'ACKNOWLEDGED', 'RESOLVED')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
```

Phase 4 persists tenant-scoped contracts and drift events. Open warning or
breach drift recalculates the mapped contract status; mapped contract statuses
roll up to `ai_systems.data_contract_status` and then into the release gate.

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

## Regulatory change monitoring (Part 14 / Flyway V16)

Assistive polled feed — not an official legal bulletin. Items are global;
reviews are tenant-scoped. Impact hints prefer `UNCERTAIN` and never auto-mutate
risk class or control status.

```sql
create table reg_sources (
  id uuid primary key,
  code varchar(64) not null unique,
  name varchar(255) not null,
  url varchar(2048) not null,
  feed_type varchar(32) not null check (feed_type in ('RSS', 'STATIC_FIXTURE', 'HTML_LIST')),
  poll_interval_seconds int not null default 900,
  enabled boolean not null default true,
  last_polled_at timestamptz,
  notes varchar(1024),
  created_at timestamptz not null
);

create table reg_items (
  id uuid primary key,
  source_id uuid not null references reg_sources(id),
  external_id varchar(512) not null,
  title varchar(1024) not null,
  summary varchar(4096) not null,
  published_at timestamptz,
  url varchar(2048) not null,
  content_hash varchar(64) not null unique,
  fetched_at timestamptz not null,
  unique (source_id, external_id)
);

create table reg_impact_hints (
  id uuid primary key,
  reg_item_id uuid not null references reg_items(id),
  control_code varchar(64),
  obligation_code varchar(64),
  impact_level varchar(32) not null check (impact_level in ('UNCERTAIN', 'POSSIBLE', 'LIKELY')),
  impact_note varchar(2048) not null,
  created_at timestamptz not null
);

create table reg_item_reviews (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  reg_item_id uuid not null references reg_items(id),
  reviewed_by uuid references users(id),
  reviewed_at timestamptz not null,
  notes varchar(2048),
  unique (tenant_id, reg_item_id)
);
```

Seeded sources include `CURATED_BOOTSTRAP` (classpath JSON fixture, enabled) plus
disabled EUR-Lex / OJ poll targets (respect ToS and rate limits when enabling).
