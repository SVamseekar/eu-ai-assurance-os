alter table users add column email_verified_at timestamp with time zone;
-- Everyone who exists today was provisioned by an operator or invite, so treat them as verified.
update users set email_verified_at = created_at where email_verified_at is null;

alter table tenants add column status varchar(24) default 'ACTIVE' not null;
alter table tenants add column deleted_at timestamp with time zone;
alter table tenants add column purge_after timestamp with time zone;

create table auth_tokens (
  id uuid primary key,
  user_id uuid not null references users(id),
  purpose varchar(32) not null,
  token_hash varchar(64) not null unique,
  expires_at timestamp with time zone not null,
  used_at timestamp with time zone,
  created_at timestamp with time zone not null
);
create index idx_auth_tokens_user_purpose on auth_tokens(user_id, purpose);
