-- Commercial onboarding + Act-as-code v2 (assisted; not legal certification)

create table user_invites (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  email varchar(320) not null,
  role varchar(64) not null,
  token_hash varchar(64) not null unique,
  invited_by uuid not null references users(id),
  expires_at timestamp with time zone not null,
  accepted_at timestamp with time zone,
  created_at timestamp with time zone not null
);

create index idx_user_invites_tenant_email on user_invites(tenant_id, email);
create index idx_user_invites_expires on user_invites(expires_at);

create table conformity_dossiers (
  id uuid primary key,
  tenant_id uuid not null references tenants(id),
  system_id uuid not null references ai_systems(id),
  annex_iv text not null,
  fria text not null,
  declaration_of_conformity text not null,
  art49_registration text not null,
  updated_at timestamp with time zone not null,
  updated_by uuid,
  unique (tenant_id, system_id)
);

create index idx_conformity_dossiers_tenant_system on conformity_dossiers(tenant_id, system_id);

insert into obligation_rules (
  id, code, title, description, legal_refs, applies_when, severity, control_codes, ruleset_version, active
) values
(
  'a1700001-0000-4000-8000-000000000001',
  'ESSENTIAL_SERVICE_ACCESS',
  'Essential private service access decisions',
  'Suggested high-risk style obligations when AI may affect access to essential private services (e.g. insurance eligibility / claims routing).',
  'Art. 6 / Annex III (essential private services) — indicative',
  '{"applicableIf":{"all":[{"field":"essential_private_service","op":"eq","value":true},{"field":"decision_impact","op":"in","value":["eligibility","access_to_service"]}]},"uncertainIf":{"all":[{"field":"essential_private_service","op":"eq","value":true},{"field":"decision_impact","op":"eq","value":"unknown"}]}}',
  'HIGH',
  '["RISK_MANAGEMENT","DATA_GOVERNANCE","RECORD_KEEPING","HUMAN_OVERSIGHT","ACCURACY_ROBUSTNESS","TECHNICAL_DOCUMENTATION","CYBERSECURITY"]',
  'v2',
  true
),
(
  'a1700001-0000-4000-8000-000000000002',
  'BIOMETRIC_IDENTIFICATION',
  'Biometric identification use',
  'Suggested elevated obligations when questionnaire indicates biometric identification use.',
  'Art. 6 / Annex III (biometrics) — indicative',
  '{"applicableIf":{"all":[{"field":"biometric","op":"eq","value":true}]},"uncertainIf":{"all":[{"field":"biometric","op":"eq","value":"unknown"}]}}',
  'HIGH',
  '["RISK_MANAGEMENT","HUMAN_OVERSIGHT","RECORD_KEEPING","TECHNICAL_DOCUMENTATION","CYBERSECURITY","DATA_GOVERNANCE"]',
  'v2',
  true
),
(
  'a1700001-0000-4000-8000-000000000003',
  'EMPLOYMENT_HR',
  'Employment and worker management',
  'Suggested high-risk style obligations for recruitment, screening, or worker evaluation use cases.',
  'Art. 6 / Annex III (employment) — indicative',
  '{"applicableIf":{"all":[{"field":"employment","op":"eq","value":true}]},"uncertainIf":{"all":[{"field":"employment","op":"eq","value":"unknown"}]}}',
  'HIGH',
  '["RISK_MANAGEMENT","DATA_GOVERNANCE","HUMAN_OVERSIGHT","TRANSPARENCY","RECORD_KEEPING","TECHNICAL_DOCUMENTATION"]',
  'v2',
  true
),
(
  'a1700001-0000-4000-8000-000000000004',
  'TRANSPARENCY_NATURAL_PERSONS',
  'Transparency when interacting with natural persons',
  'Suggested Art. 50-style transparency when the system interacts with natural persons.',
  'Art. 50 — indicative (in force 2 Aug 2026)',
  '{"applicableIf":{"any":[{"field":"interacts_with_natural_persons","op":"eq","value":true},{"field":"art50_chatbot","op":"eq","value":true}]}}',
  'MEDIUM',
  '["TRANSPARENCY"]',
  'v2',
  true
),
(
  'a1700001-0000-4000-8000-000000000005',
  'DATA_GOVERNANCE_PROFILING',
  'Data governance for profiling inputs',
  'Suggested data governance controls when profiling or automated scoring of persons is indicated.',
  'Art. 10-style data governance — indicative',
  '{"applicableIf":{"all":[{"field":"profiling","op":"eq","value":true}]},"uncertainIf":{"all":[{"field":"profiling","op":"eq","value":"unknown"}]}}',
  'MEDIUM',
  '["DATA_GOVERNANCE","RECORD_KEEPING"]',
  'v2',
  true
),
(
  'a1700001-0000-4000-8000-000000000006',
  'HUMAN_OVERSIGHT_HIGH_IMPACT',
  'Human oversight for high-impact decisions',
  'Suggested human oversight when decision impact is eligibility/access or human-in-the-loop is required but not confirmed.',
  'Art. 14-style human oversight — indicative',
  '{"applicableIf":{"any":[{"field":"decision_impact","op":"in","value":["eligibility","access_to_service","employment"]},{"all":[{"field":"human_in_loop","op":"eq","value":false},{"field":"essential_private_service","op":"eq","value":true}]}]},"uncertainIf":{"all":[{"field":"human_in_loop","op":"eq","value":"unknown"}]}}',
  'HIGH',
  '["HUMAN_OVERSIGHT"]',
  'v2',
  true
),
(
  'a1700001-0000-4000-8000-000000000007',
  'BASELINE_GOVERNANCE',
  'Baseline governance for registered AI systems',
  'Baseline suggested governance for any registered AI system in the assurance OS (always mapped for inventory completeness).',
  'Internal control catalog baseline — not a legal classification',
  '{"applicableIf":{"all":[]}}',
  'LOW',
  '["RISK_MANAGEMENT","DATA_GOVERNANCE"]',
  'v2',
  true
),
(
  'a1700001-0000-4000-8000-000000000008',
  'HIGH_RISK_BUNDLE_SELF_ASSESSED',
  'Self-assessed high-risk bundle',
  'When the questionnaire indicates operator self-assessment as high-risk, map the full high-risk control bundle for review.',
  'Operator self-assessment input — not a legal determination',
  '{"applicableIf":{"all":[{"field":"high_risk_self_assessment","op":"eq","value":true}]},"uncertainIf":{"all":[{"field":"high_risk_self_assessment","op":"eq","value":"unknown"}]}}',
  'HIGH',
  '["RISK_MANAGEMENT","DATA_GOVERNANCE","RECORD_KEEPING","TRANSPARENCY","HUMAN_OVERSIGHT","ACCURACY_ROBUSTNESS","CYBERSECURITY","TECHNICAL_DOCUMENTATION"]',
  'v2',
  true
),
(
  'a1700001-0000-4000-8000-000000000009',
  'PROHIBITED_SOCIAL_SCORING',
  'Prohibited social scoring screen',
  'Suggested BLOCK if the system scores natural persons for social behaviour in a prohibited-practice pattern. Human legal review required.',
  'Art. 5(1)(c) — indicative prohibited-practice screen',
  '{"applicableIf":{"all":[{"field":"prohibited_social_scoring","op":"eq","value":true}]},"uncertainIf":{"all":[{"field":"prohibited_social_scoring","op":"eq","value":"unknown"}]}}',
  'HIGH',
  '["RISK_MANAGEMENT","HUMAN_OVERSIGHT"]',
  'v2',
  true
),
(
  'a1700001-0000-4000-8000-000000000010',
  'PROHIBITED_EMOTION_WORKPLACE',
  'Prohibited workplace emotion recognition screen',
  'Suggested BLOCK if emotion recognition is used in the workplace or education except where the Act allows.',
  'Art. 5(1)(f) — indicative prohibited-practice screen',
  '{"applicableIf":{"all":[{"field":"prohibited_emotion_workplace","op":"eq","value":true}]},"uncertainIf":{"all":[{"field":"prohibited_emotion_workplace","op":"eq","value":"unknown"}]}}',
  'HIGH',
  '["RISK_MANAGEMENT","HUMAN_OVERSIGHT"]',
  'v2',
  true
),
(
  'a1700001-0000-4000-8000-000000000011',
  'PROHIBITED_SUBLIMINAL',
  'Prohibited subliminal / manipulative technique screen',
  'Suggested BLOCK if subliminal or purposefully manipulative techniques are used to distort behaviour.',
  'Art. 5(1)(a) — indicative prohibited-practice screen',
  '{"applicableIf":{"all":[{"field":"prohibited_subliminal","op":"eq","value":true}]},"uncertainIf":{"all":[{"field":"prohibited_subliminal","op":"eq","value":"unknown"}]}}',
  'HIGH',
  '["RISK_MANAGEMENT"]',
  'v2',
  true
),
(
  'a1700001-0000-4000-8000-000000000012',
  'PROHIBITED_BIOMETRIC_REALTIME',
  'Prohibited real-time remote biometric identification in public',
  'Suggested BLOCK if real-time remote biometric identification is used in publicly accessible spaces outside narrow exceptions.',
  'Art. 5(1)(h) — indicative prohibited-practice screen',
  '{"applicableIf":{"all":[{"field":"prohibited_biometric_realtime","op":"eq","value":true}]},"uncertainIf":{"all":[{"field":"prohibited_biometric_realtime","op":"eq","value":"unknown"}]}}',
  'HIGH',
  '["RISK_MANAGEMENT","CYBERSECURITY","HUMAN_OVERSIGHT"]',
  'v2',
  true
),
(
  'a1700001-0000-4000-8000-000000000013',
  'ART50_SYNTHETIC_CONTENT',
  'Art. 50 synthetic content marking',
  'Suggested transparency / marking duties when the system generates synthetic audio, image, video, or text.',
  'Art. 50(2) — indicative (in force 2 Aug 2026; legacy marking 2 Dec 2026)',
  '{"applicableIf":{"all":[{"field":"art50_synthetic","op":"eq","value":true}]},"uncertainIf":{"all":[{"field":"art50_synthetic","op":"eq","value":"unknown"}]}}',
  'MEDIUM',
  '["TRANSPARENCY","RECORD_KEEPING"]',
  'v2',
  true
),
(
  'a1700001-0000-4000-8000-000000000014',
  'PROVIDER_ANNEX_IV',
  'Provider technical documentation (Annex IV-shaped)',
  'When the operator is a provider of a high-risk-shaped system, map technical documentation completeness.',
  'Art. 11 / Annex IV — indicative checklist, not a legal file',
  '{"applicableIf":{"all":[{"field":"operator_role","op":"eq","value":"provider"},{"field":"high_risk_self_assessment","op":"eq","value":true}]}}',
  'HIGH',
  '["TECHNICAL_DOCUMENTATION","RECORD_KEEPING"]',
  'v2',
  true
),
(
  'a1700001-0000-4000-8000-000000000015',
  'DEPLOYER_INSTRUCTIONS',
  'Deployer human oversight and instructions',
  'When the operator is a deployer, map human oversight and instruction-following evidence.',
  'Art. 26 deployer-style duties — indicative',
  '{"applicableIf":{"all":[{"field":"operator_role","op":"eq","value":"deployer"}]}}',
  'MEDIUM',
  '["HUMAN_OVERSIGHT","TRANSPARENCY"]',
  'v2',
  true
),
(
  'a1700001-0000-4000-8000-000000000016',
  'GPAI_TRANSPARENCY',
  'GPAI / general-purpose model documentation',
  'Suggested documentation when the system is or embeds a general-purpose AI model.',
  'Art. 53 GPAI-style duties — indicative',
  '{"applicableIf":{"all":[{"field":"gpai_model","op":"eq","value":true}]},"uncertainIf":{"all":[{"field":"gpai_model","op":"eq","value":"unknown"}]}}',
  'MEDIUM',
  '["TECHNICAL_DOCUMENTATION","TRANSPARENCY","RECORD_KEEPING"]',
  'v2',
  true
);
