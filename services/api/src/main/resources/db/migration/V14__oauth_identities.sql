-- Part 4: OAuth/OIDC identity binding for Google + Microsoft.
-- password_hash remains nullable (V9) so OAuth-only users are valid.
-- Unique (provider, subject): multiple password-only rows keep both columns NULL
-- (Postgres and H2 PostgreSQL mode treat NULLs as distinct in unique indexes).

alter table users add column oauth_provider varchar(32);
alter table users add column oauth_subject varchar(255);

create unique index uq_users_oauth_provider_subject
  on users (oauth_provider, oauth_subject);
