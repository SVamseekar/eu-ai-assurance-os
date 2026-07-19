-- Part 14: Regulatory change monitoring feed (polled assistive feed, not official legal bulletin)

create table reg_sources (
  id uuid primary key,
  code varchar(64) not null unique,
  name varchar(255) not null,
  url varchar(2048) not null,
  feed_type varchar(32) not null check (feed_type in ('RSS', 'STATIC_FIXTURE', 'HTML_LIST')),
  poll_interval_seconds int not null default 900,
  enabled boolean not null default true,
  last_polled_at timestamp with time zone,
  notes varchar(1024),
  created_at timestamp with time zone not null
);

create index idx_reg_sources_enabled on reg_sources(enabled);

create table reg_items (
  id uuid primary key,
  source_id uuid not null references reg_sources(id),
  external_id varchar(512) not null,
  title varchar(1024) not null,
  summary varchar(4096) not null,
  published_at timestamp with time zone,
  url varchar(2048) not null,
  content_hash varchar(64) not null,
  fetched_at timestamp with time zone not null,
  unique (source_id, external_id),
  unique (content_hash)
);

create index idx_reg_items_fetched_at on reg_items(fetched_at desc);
create index idx_reg_items_published_at on reg_items(published_at desc);
create index idx_reg_items_source on reg_items(source_id);

create table reg_impact_hints (
  id uuid primary key,
  reg_item_id uuid not null references reg_items(id),
  control_code varchar(64),
  obligation_code varchar(64),
  impact_level varchar(32) not null check (impact_level in ('UNCERTAIN', 'POSSIBLE', 'LIKELY')),
  impact_note varchar(2048) not null,
  created_at timestamp with time zone not null
);

create index idx_reg_impact_hints_item on reg_impact_hints(reg_item_id);
create index idx_reg_impact_hints_control on reg_impact_hints(control_code);
create index idx_reg_impact_hints_obligation on reg_impact_hints(obligation_code);

-- Tenant-scoped human review state. Never auto-mutates risk class or control status.
create table reg_item_reviews (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  reg_item_id uuid not null references reg_items(id),
  reviewed_by uuid references users(id),
  reviewed_at timestamp with time zone not null,
  notes varchar(2048),
  unique (tenant_id, reg_item_id)
);

create index idx_reg_item_reviews_tenant on reg_item_reviews(tenant_id, reviewed_at desc);

-- Curated sources (v1). EUR-Lex / OJ URLs are documented poll targets; rate limits and ToS apply.
-- STATIC_FIXTURE source is always available when network is blocked.
insert into reg_sources (
  id, code, name, url, feed_type, poll_interval_seconds, enabled, last_polled_at, notes, created_at
) values
(
  'a1600001-0000-4000-8000-000000000001',
  'CURATED_BOOTSTRAP',
  'Curated bootstrap fixture (offline)',
  'classpath:reg-monitor/bootstrap-feed.json',
  'STATIC_FIXTURE',
  3600,
  true,
  null,
  'In-repo curated items for local/H2 and network-blocked environments. Assistive only.',
  '2026-07-20T00:00:00Z'
),
(
  'a1600001-0000-4000-8000-000000000002',
  'EURLEX_AI_ACT',
  'EUR-Lex AI Act search (poll target)',
  'https://eur-lex.europa.eu/search.html?scope=EURLEX&text=artificial+intelligence&lang=en&type=quick&qid=',
  'HTML_LIST',
  3600,
  false,
  null,
  'Disabled by default. Respect EUR-Lex ToS and rate limits. Not an official legal bulletin subscription.',
  '2026-07-20T00:00:00Z'
),
(
  'a1600001-0000-4000-8000-000000000003',
  'OJ_RSS',
  'EU Official Journal RSS (poll target)',
  'https://eur-lex.europa.eu/EN/display-feed.html?rssId=oj-l',
  'RSS',
  1800,
  false,
  null,
  'Disabled by default. Near-real-time poll only when enabled. Document latency as poll interval, not live law.',
  '2026-07-20T00:00:00Z'
);
