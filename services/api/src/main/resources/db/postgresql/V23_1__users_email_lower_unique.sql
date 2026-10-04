-- Postgres only: H2 has no expression indexes.
-- Fails the deploy with a readable message, before any index is built, if two accounts share an address
-- (ignoring case). Resolve those rows by hand, then redeploy.
do $$
declare
  dupes text;
begin
  select string_agg(addr || ' (' || n || ')', ', ') into dupes
  from (select lower(email) as addr, count(*) as n from users group by 1 having count(*) > 1) d;
  if dupes is not null then
    raise exception 'Cannot enforce one account per email address. Duplicate addresses: %', dupes;
  end if;
end $$;

create unique index idx_users_email_lower on users(lower(email));
