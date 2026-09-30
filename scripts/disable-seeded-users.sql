-- Disables password login for the five demo accounts seeded by older builds.
-- Safe to re-run. Rows are kept because audit events and approvals reference them.
update users
   set password_hash = null
 where lower(email) in (
   'compliance@example.com',
   'engineering@example.com',
   'auditor@example.com',
   'legal@example.com',
   'admin@example.com');

delete from api_keys where id = '00000000-0000-0000-0000-000000000a01';
