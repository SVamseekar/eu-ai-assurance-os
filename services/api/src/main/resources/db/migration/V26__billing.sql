create table tenant_subscriptions (
  tenant_id uuid primary key references tenants(id),
  plan_code varchar(32) not null,
  status varchar(24) not null,
  billing_interval varchar(8),
  dodo_customer_id varchar(64),
  dodo_subscription_id varchar(64) unique,
  current_period_end timestamp with time zone,
  grace_until timestamp with time zone,
  updated_at timestamp with time zone not null
);

create table billing_webhook_events (
  webhook_id varchar(128) primary key,
  event_type varchar(64) not null,
  payload_sha256 varchar(64) not null,
  received_at timestamp with time zone not null
);

create table gate_run_counters (
  tenant_id uuid not null references tenants(id),
  period varchar(7) not null,
  runs integer not null,
  primary key (tenant_id, period)
);

alter table tenants add column trial_ends_at timestamp with time zone;
update tenants set trial_ends_at = created_at + interval '14' day where plan = 'trial';
