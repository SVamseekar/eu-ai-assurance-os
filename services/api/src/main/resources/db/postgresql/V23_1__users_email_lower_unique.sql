-- Postgres only: H2 has no expression indexes. Before deploying, this must return no rows:
--   select lower(email), count(*) from users group by 1 having count(*) > 1;
create unique index idx_users_email_lower on users(lower(email));
