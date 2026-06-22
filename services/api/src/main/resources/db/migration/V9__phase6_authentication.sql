alter table users add column password_hash varchar(255);

create table refresh_tokens (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  user_id uuid not null references users(id),
  token_hash varchar(64) not null unique,
  expires_at timestamp with time zone not null,
  created_at timestamp with time zone not null,
  revoked_at timestamp with time zone,
  replaced_by_token_hash varchar(64)
);

create index idx_refresh_tokens_token_hash on refresh_tokens(token_hash);
create index idx_refresh_tokens_user_id on refresh_tokens(tenant_id, user_id);

create table signing_keys (
  kid uuid primary key,
  algorithm varchar(16) not null,
  public_key_pem text not null,
  private_key_pem text not null,
  created_at timestamp with time zone not null,
  active boolean not null default false
);