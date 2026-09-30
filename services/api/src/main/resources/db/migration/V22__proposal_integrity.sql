alter table mapping_proposals add column version bigint default 0 not null;

create index idx_assessment_applicability_proposal on assessment_applicability(proposal_id);
create index idx_evidence_exceptions_proposal_only on evidence_exceptions(proposal_id);
create index idx_mapping_proposals_decided_by on mapping_proposals(decided_by);
