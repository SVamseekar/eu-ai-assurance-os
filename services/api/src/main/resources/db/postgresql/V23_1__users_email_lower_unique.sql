-- Postgres only: H2 has no expression indexes.
-- Fails the deploy with a readable message, before any index is built, if two accounts share an address
-- (ignoring case). Resolve those rows by hand, then redeploy. The message gives a count and the query to run,
-- not the addresses themselves, so personal data stays out of deploy logs.
do $$
declare
  dupes integer;
begin
  select count(*) into dupes
  from (select 1 from users group by lower(email) having count(*) > 1) d;
  if dupes > 0 then
    raise exception 'Cannot enforce one account per email address: % address(es) are shared by several accounts. '
      'Find them with: select lower(email), count(*) from users group by 1 having count(*) > 1', dupes;
  end if;
end $$;

create unique index idx_users_email_lower on users(lower(email));
