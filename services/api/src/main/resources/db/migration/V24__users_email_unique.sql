-- One account per address on every database. Signup relies on this to serialize parallel
-- registrations of a new address (Postgres also has the case-insensitive index in db/postgresql).
create unique index idx_users_email_unique on users (email);
