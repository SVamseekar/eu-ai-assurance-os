-- Plan 08 Task 5: record which DPA Cover Page version a workspace admin accepted, and when.
alter table tenants add column dpa_accepted_at timestamp with time zone;
alter table tenants add column dpa_version varchar(16);
