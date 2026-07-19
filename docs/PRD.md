# Product Requirements Document

## 1. Vision

EU AI Assurance OS helps organizations release AI systems in Europe with evidence, controls, and auditability. It acts as a release control plane that validates each AI system against EU AI Act-style risk obligations, GDPR privacy expectations, data governance checks, model evaluation gates, and human oversight workflows.

## 2. Target Users

- AI Engineering Lead: needs release gates for model and prompt changes.
- Compliance Officer: needs evidence that AI systems meet regulatory obligations.
- Legal Counsel: needs documented risk classification, oversight, and audit trails.
- Data Platform Lead: needs data-contract drift and lineage checks.
- Product Owner: needs a clear pass/review/block decision before launch.
- Auditor: needs immutable evidence of who approved what and why.

## 3. Primary Use Case

A European insurance provider wants to deploy Claims Triage AI. The system helps prioritize claims, recommend next actions, and route cases to reviewers. Because it can affect access to an essential private service, it is treated as high-risk.

Before release, the team must:

- Register the system and ownership.
- Classify risk and record the basis.
- Attach evidence such as DPIA, model card, policy docs, vendor docs, and data contracts.
- Run eval gates for faithfulness, bias, refusal behavior, accuracy, latency, and cost.
- Confirm data inputs have approved contracts and no drift.
- Require human oversight approval.
- Export an evidence pack for audit review.

## 4. MVP Scope

In scope:

- Multi-tenant AI system registry.
- Risk classification workflow.
- Control checklist mapped to EU AI Act-style obligations.
- Evidence document inventory and cited evidence answers.
- Eval run records and release gate decisioning.
- Data contract monitor status.
- Audit event timeline.
- Evidence pack export.

Out of scope for MVP (as final authority / overclaim):

- Full legal determination engine as legal advice or official conformity assessment.
- Automated certification claims.
- Real-time regulatory change monitoring as a live legal database.
- Complex live vendor sector connectors without SPI stubs.
- Production SSO beyond the implemented OAuth/basic role model (see Phase 6).

Phase 7 product expansions (assisted — never final legal authority):

- **Assisted obligation determination** (Part 12): questionnaire + deterministic ruleset → suggested applicability / obligation map mapped to control codes. Always labeled not legal advice; requires human legal review; never auto-changes risk class without human confirm.
- Certification readiness score (Part 13), regulatory change feed (Part 14), sector SPI packs (Part 15).

## 5. Core Features

### AI System Registry

Records every AI system with owner, purpose, risk class, deployment context, vendor/model info, data sources, and release status.

### Risk Classification

Guided workflow records risk class, rationale, affected users, sector, decision impact, and required controls.

### Evidence RAG

Retrieves cited answers from policy docs, DPIAs, model cards, vendor docs, incident records, data contracts, and control mappings.

### Eval Gates

Stores eval datasets, model/prompt versions, scores, thresholds, guard metrics, and release decisions.

### Data Contract Monitor

Tracks input schemas, semantic contracts, lineage, drift events, severity, and remediation state.

### Approval Workflow

Routes blocked or review systems through owner, compliance, legal, and human oversight approvals.

### Audit Ledger

Append-only log of risk decisions, evidence answers, eval runs, approvals, overrides, exports, and data drift events.

## 6. MVP Acceptance Criteria

- A user can create an AI system and assign risk class.
- The system calculates release decision as Pass, Review, or Blocked.
- A high-risk blocked system explains the blocking controls.
- An evidence query returns answer text with citations.
- An eval run updates the release gate.
- A data-contract breach blocks or reopens a release review.
- An evidence pack can be exported as sealed JSON (`contentSha256`).
- PDF evidence pack export is available as Phase 6 polish (same seal; JSON remains primary).
- Audit entries are created for all critical actions.

## 7. Non-Functional Requirements

- p95 API latency under 300 ms for registry reads.
- p95 RAG answer latency under 4 seconds for MVP.
- 99.9% uptime target for production.
- Tenant data isolation.
- Encryption in transit and at rest.
- Audit event retention configurable for at least 7 years.
- Evidence pack export must be deterministic and traceable.

## 8. Success Metrics

- 80% of AI systems have complete owner, risk, and evidence metadata.
- 95% of releases have documented gate decisions.
- 100% of high-risk systems have audit trail coverage.
- Eval regression catch rate above 90% on critical datasets.
- Mean time to produce audit evidence under 30 minutes.

## 9. Monetization

Potential SaaS plans:

- Starter: limited systems, evidence inventory, manual exports.
- Business: eval gates, data contracts, workflows, integrations.
- Enterprise: SSO, custom controls, on-prem or VPC deployment, immutable audit storage, advanced reporting.

## 10. Source PRD Mapping

- ComplianceGuard RAG contributes cited evidence and EU compliance readiness.
- EvalForge contributes eval gates, LLM-as-judge concepts, CI/CD decisioning.
- Data Contracts AI contributes drift and lineage controls.
- Java Spring Boot regulated-system PRDs contribute auditability, SAGA workflows, idempotency, security, and multi-tenancy.
