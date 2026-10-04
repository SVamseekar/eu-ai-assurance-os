alter table api_keys add column name varchar(80);
alter table api_keys add column prefix varchar(16);
alter table api_keys add column last_used_at timestamp with time zone;
alter table api_keys add column revoked_at timestamp with time zone;
update api_keys set name = 'Legacy key', prefix = 'legacy' where name is null;
