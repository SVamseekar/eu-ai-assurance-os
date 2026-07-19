-- Part 12: Assisted obligation determination engine (not legal advice)

create table obligation_rules (
  id uuid primary key,
  code varchar(64) not null,
  title varchar(255) not null,
  description varchar(2048) not null,
  legal_refs varchar(512) not null,
  applies_when text not null,
  severity varchar(32) not null check (severity in ('LOW', 'MEDIUM', 'HIGH')),
  control_codes text not null,
  ruleset_version varchar(32) not null,
  active boolean not null default true,
  unique (code, ruleset_version)
);

create index idx_obligation_rules_version_active
  on obligation_rules(ruleset_version, active);

create table determination_runs (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  system_id uuid not null references ai_systems(id),
  questionnaire_json text not null,
  result_json text not null,
  status varchar(32) not null check (status in ('COMPLETED', 'FAILED')),
  ruleset_version varchar(32) not null,
  created_by uuid references users(id),
  created_at timestamp with time zone not null
);

create index idx_determination_runs_tenant_system
  on determination_runs(tenant_id, system_id, created_at);

create table determination_obligations (
  id uuid primary key,
  run_id uuid not null references determination_runs(id),
  rule_code varchar(64) not null,
  applicability varchar(32) not null check (applicability in ('APPLICABLE', 'NOT_APPLICABLE', 'UNCERTAIN')),
  rationale varchar(2048) not null,
  control_codes text not null,
  legal_refs varchar(512) not null,
  title varchar(255) not null,
  severity varchar(32) not null
);

create index idx_determination_obligations_run
  on determination_obligations(run_id);

-- Seed ruleset v1 (questionnaire-driven; not a legal determination)
-- Fixed UUIDs for deterministic local/H2 smoke tests.

insert into obligation_rules (
  id, code, title, description, legal_refs, applies_when, severity, control_codes, ruleset_version, active
) values
(
  'a1500001-0000-4000-8000-000000000001',
  'ESSENTIAL_SERVICE_ACCESS',
  'Essential private service access decisions',
  'Suggested high-risk style obligations when AI may affect access to essential private services (e.g. insurance eligibility / claims routing).',
  'Art. 6 / Annex III (essential private services) — indicative',
  '{"applicableIf":{"all":[{"field":"essential_private_service","op":"eq","value":true},{"field":"decision_impact","op":"in","value":["eligibility","access_to_service"]}]},"uncertainIf":{"all":[{"field":"essential_private_service","op":"eq","value":true},{"field":"decision_impact","op":"eq","value":"unknown"}]}}',
  'HIGH',
  '["RISK_MANAGEMENT","DATA_GOVERNANCE","RECORD_KEEPING","HUMAN_OVERSIGHT","ACCURACY_ROBUSTNESS","TECHNICAL_DOCUMENTATION","CYBERSECURITY"]',
  'v1',
  true
),
(
  'a1500001-0000-4000-8000-000000000002',
  'BIOMETRIC_IDENTIFICATION',
  'Biometric identification use',
  'Suggested elevated obligations when questionnaire indicates biometric identification use.',
  'Art. 6 / Annex III (biometrics) — indicative',
  '{"applicableIf":{"all":[{"field":"biometric","op":"eq","value":true}]},"uncertainIf":{"all":[{"field":"biometric","op":"eq","value":"unknown"}]}}',
  'HIGH',
  '["RISK_MANAGEMENT","HUMAN_OVERSIGHT","RECORD_KEEPING","TECHNICAL_DOCUMENTATION","CYBERSECURITY","DATA_GOVERNANCE"]',
  'v1',
  true
),
(
  'a1500001-0000-4000-8000-000000000003',
  'EMPLOYMENT_HR',
  'Employment and worker management',
  'Suggested high-risk style obligations for recruitment, screening, or worker evaluation use cases.',
  'Art. 6 / Annex III (employment) — indicative',
  '{"applicableIf":{"all":[{"field":"employment","op":"eq","value":true}]},"uncertainIf":{"all":[{"field":"employment","op":"eq","value":"unknown"}]}}',
  'HIGH',
  '["RISK_MANAGEMENT","DATA_GOVERNANCE","HUMAN_OVERSIGHT","TRANSPARENCY","RECORD_KEEPING","TECHNICAL_DOCUMENTATION"]',
  'v1',
  true
),
(
  'a1500001-0000-4000-8000-000000000004',
  'TRANSPARENCY_NATURAL_PERSONS',
  'Transparency when interacting with natural persons',
  'Suggested transparency obligations when the system interacts with natural persons or generates content that may be mistaken for human output.',
  'Art. 50 / transparency-style duties — indicative',
  '{"applicableIf":{"all":[{"field":"interacts_with_natural_persons","op":"eq","value":true}]}}',
  'MEDIUM',
  '["TRANSPARENCY"]',
  'v1',
  true
),
(
  'a1500001-0000-4000-8000-000000000005',
  'DATA_GOVERNANCE_PROFILING',
  'Data governance for profiling inputs',
  'Suggested data governance controls when profiling or automated scoring of persons is indicated.',
  'Art. 10-style data governance — indicative',
  '{"applicableIf":{"all":[{"field":"profiling","op":"eq","value":true}]},"uncertainIf":{"all":[{"field":"profiling","op":"eq","value":"unknown"}]}}',
  'MEDIUM',
  '["DATA_GOVERNANCE","RECORD_KEEPING"]',
  'v1',
  true
),
(
  'a1500001-0000-4000-8000-000000000006',
  'HUMAN_OVERSIGHT_HIGH_IMPACT',
  'Human oversight for high-impact decisions',
  'Suggested human oversight when decision impact is eligibility/access or human-in-the-loop is required but not confirmed.',
  'Art. 14-style human oversight — indicative',
  '{"applicableIf":{"any":[{"field":"decision_impact","op":"in","value":["eligibility","access_to_service","employment"]},{"all":[{"field":"human_in_loop","op":"eq","value":false},{"field":"essential_private_service","op":"eq","value":true}]}]},"uncertainIf":{"all":[{"field":"human_in_loop","op":"eq","value":"unknown"}]}}',
  'HIGH',
  '["HUMAN_OVERSIGHT"]',
  'v1',
  true
),
(
  'a1500001-0000-4000-8000-000000000007',
  'BASELINE_GOVERNANCE',
  'Baseline governance for registered AI systems',
  'Baseline suggested governance for any registered AI system in the assurance OS (always mapped for inventory completeness).',
  'Internal control catalog baseline — not a legal classification',
  '{"applicableIf":{"all":[]}}',
  'LOW',
  '["RISK_MANAGEMENT","DATA_GOVERNANCE"]',
  'v1',
  true
),
(
  'a1500001-0000-4000-8000-000000000008',
  'HIGH_RISK_BUNDLE_SELF_ASSESSED',
  'Self-assessed high-risk bundle',
  'When the questionnaire indicates operator self-assessment as high-risk, map the full high-risk control bundle for review.',
  'Operator self-assessment input — not a legal determination',
  '{"applicableIf":{"all":[{"field":"high_risk_self_assessment","op":"eq","value":true}]},"uncertainIf":{"all":[{"field":"high_risk_self_assessment","op":"eq","value":"unknown"}]}}',
  'HIGH',
  '["RISK_MANAGEMENT","DATA_GOVERNANCE","RECORD_KEEPING","TRANSPARENCY","HUMAN_OVERSIGHT","ACCURACY_ROBUSTNESS","CYBERSECURITY","TECHNICAL_DOCUMENTATION"]',
  'v1',
  true
);
