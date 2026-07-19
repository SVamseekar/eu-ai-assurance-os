alter table ai_systems
  add column vendor_name varchar(255);

alter table ai_systems
  add column model_name varchar(255);

alter table ai_systems
  add column model_version varchar(128);

alter table ai_systems
  add column data_sources_json text not null default '[]';

alter table ai_systems
  add column sector varchar(128);

alter table ai_systems
  add column decision_impact varchar(512);

alter table ai_systems
  add column affected_users_json text not null default '[]';
