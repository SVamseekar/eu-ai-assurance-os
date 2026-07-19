export type RiskClass = "minimal" | "limited" | "high" | "prohibited";
export type ReleaseDecision = "pass" | "review" | "blocked" | "PASS" | "REVIEW" | "BLOCKED";
export type DataContractStatus = "HEALTHY" | "WARNING" | "BREACH";
export type DriftSeverity = "INFO" | "WARNING" | "BREACH";
export type DriftStatus = "OPEN" | "ACKNOWLEDGED" | "RESOLVED";
export type EvalRunStatus = "queued" | "running" | "completed" | "failed";
export type IngestionStatus = "indexed" | "pending" | "failed";

export interface AiSystem {
  id: string;
  name: string;
  owner: string;
  purpose: string;
  riskClass: RiskClass;
  riskBasis: string;
  deploymentRegion: string;
  evidenceCoverage: number;
  evalScore: number;
  dataContractStatus: DataContractStatus;
  releaseDecision: ReleaseDecision;
  openGaps: string[];
  vendorName?: string | null;
  modelName?: string | null;
  modelVersion?: string | null;
  dataSources?: string[];
  sector?: string | null;
  decisionImpact?: string | null;
  affectedUsers?: string[];
  createdAt: string;
  updatedAt: string;
}

export type ControlStatus = "PASS" | "REVIEW" | "BLOCKED";

export interface Control {
  id: string;
  code: string;
  name: string;
  description: string;
  appliesToRiskClass: string;
  category: string;
}

export interface SystemControl {
  id: string;
  systemId: string;
  controlId: string;
  controlCode: string;
  controlName: string;
  category: string;
  status: ControlStatus;
  evidenceRequired: boolean;
  reviewerId: string | null;
  notes: string | null;
  updatedAt: string;
}

export interface EvidenceDocument {
  id: string;
  systemId: string;
  type: string;
  title: string;
  sourceUri: string;
  checksum: string;
  chunkCount: number;
  ingestionStatus: IngestionStatus;
  createdAt: string;
}

export interface Citation {
  documentId: string;
  title: string;
  section: string;
  snippet: string;
}

export interface EvidenceQueryResponse {
  answer: string;
  confidence: number;
  citations: Citation[];
}

export interface EvalRunMetrics {
  faithfulness?: number;
  relevance?: number;
  safetyRefusal?: number;
  biasSlicePassRate?: number;
  latencyP95Ms?: number;
  costUsd?: number;
}

export interface EvalRun {
  runId: string;
  systemId: string;
  datasetId: string | null;
  status: EvalRunStatus;
  dataset: string;
  modelVersion: string;
  promptVersion: string;
  threshold: number;
  metrics: EvalRunMetrics;
  releaseDecision: ReleaseDecision;
  createdAt: string;
  queuedAt: string;
  startedAt: string | null;
  completedAt: string | null;
  failedAt: string | null;
  workerAttempts: number;
  maxAttempts: number;
  failureReason: string | null;
}

export interface EvalRunOperationsView {
  queued: EvalRun[];
  running: EvalRun[];
  retryable: EvalRun[];
  deadLetter: EvalRun[];
}

export interface DataContract {
  id: string;
  systemId: string;
  name: string;
  owner: string;
  version: string;
  status: DataContractStatus;
  coverage: number;
  createdAt: string;
  updatedAt: string;
}

export interface DriftEvent {
  id: string;
  contractId: string;
  severity: DriftSeverity;
  field: string | null;
  description: string;
  status: DriftStatus;
  createdAt: string;
  updatedAt: string;
}

export interface AuditEvent {
  id: string;
  systemId: string | null;
  actorId: string | null;
  eventType: string;
  resourceType: string;
  resourceId: string | null;
  payload: Record<string, unknown>;
  createdAt: string;
  prevEventHash?: string | null;
  eventHash?: string | null;
  retainUntil?: string | null;
}

export interface ReleaseGateResponse {
  systemId: string;
  decision: ReleaseDecision;
  blockers: string[];
}

/** Sealed evidence pack (JSON primary; PDF is Phase 6 export). */
export interface EvidencePack {
  systemId: string;
  generatedAt: string;
  decision: ReleaseDecision;
  riskClassification: Record<string, unknown>;
  evidence: Record<string, unknown>[];
  evalRuns: Record<string, unknown>[];
  dataContracts: Record<string, unknown>[];
  approvals: Record<string, unknown>[];
  auditEvents: AuditEvent[];
  /** Assisted obligation determination snapshot (not legal advice). */
  determination?: Record<string, unknown>;
  evidencePackVersion: string;
  contentSha256: string;
  generator: string;
  auditChainHead: string | null;
}

export type ObligationApplicability = "APPLICABLE" | "NOT_APPLICABLE" | "UNCERTAIN";

export interface DeterminationQuestionOption {
  value: string;
  label: string;
}

export interface DeterminationQuestion {
  id: string;
  label: string;
  help: string;
  type: string;
  required: boolean;
  options: DeterminationQuestionOption[];
}

export interface DeterminationQuestionnaire {
  rulesetVersion: string;
  disclaimer: string;
  productLabel: string;
  questions: DeterminationQuestion[];
}

export interface DeterminationObligationItem {
  id: string | null;
  ruleCode: string;
  title: string;
  applicability: ObligationApplicability;
  rationale: string;
  controlCodes: string[];
  legalRefs: string;
  severity: string;
}

/** Certification readiness only — never legal certification status. */
export type CertificationReadinessStatus = "NOT_READY" | "READY_FOR_REVIEW" | "GAPS";
export type ReadinessGapSeverity = "CRITICAL" | "HIGH" | "MEDIUM" | "LOW";

export interface ReadinessGap {
  code: string;
  severity: ReadinessGapSeverity;
  message: string;
  remediationHint: string;
  dimension: string;
}

export interface ReadinessDimensionScore {
  code: string;
  label: string;
  weight: number;
  score: number;
  weightedPoints: number;
  status: string;
  summary: string;
}

export interface CertificationReadiness {
  systemId: string;
  systemName: string;
  score: number;
  readinessStatus: CertificationReadinessStatus;
  productLabel: string;
  disclaimer: string;
  generatedAt: string;
  releaseDecision: ReleaseDecision;
  dimensions: ReadinessDimensionScore[];
  gaps: ReadinessGap[];
}

/** Regulatory change monitoring (Part 14) — assistive polled feed, not official bulletin. */
export type RegImpactLevel = "UNCERTAIN" | "POSSIBLE" | "LIKELY";

export interface RegImpactHint {
  id: string;
  regItemId: string;
  controlCode: string | null;
  obligationCode: string | null;
  impactLevel: RegImpactLevel;
  impactNote: string;
  createdAt: string;
}

/** Sector packs (Part 15) — SPI + 3 verticals, not all industries integrated. */
export interface SectorControlDef {
  code: string;
  name: string;
  description: string;
  appliesToRiskClass: string;
  category: string;
}

export interface SampleEvidenceTemplate {
  id: string;
  title: string;
  documentType: string;
  resourcePath: string;
  description: string;
}

export interface SectorPack {
  id: string;
  displayName: string;
  summary: string;
  sectorKeys: string[];
  extraControls: SectorControlDef[];
  questionnaireDefaults: Record<string, unknown>;
  sampleEvidenceTemplates: SampleEvidenceTemplate[];
  metricsLabel: string;
  disclaimer: string;
}

export interface SectorPacksResponse {
  packs: SectorPack[];
  metricsLabel: string;
  disclaimer: string;
  notAllIndustriesNote: string;
}

export interface RegItem {
  id: string;
  sourceId: string;
  sourceCode: string;
  externalId: string;
  title: string;
  summary: string;
  publishedAt: string | null;
  url: string;
  contentHash: string;
  fetchedAt: string;
  impactHints: RegImpactHint[];
  reviewed: boolean;
  reviewedAt: string | null;
  reviewNotes: string | null;
  relevanceReason?: string | null;
  productLabel: string;
  disclaimer: string;
}

export interface RegMonitorFeed {
  productLabel: string;
  disclaimer: string;
  latencyNote: string;
  items: RegItem[];
}

export interface DeterminationRun {
  id: string;
  systemId: string;
  questionnaire: Record<string, unknown>;
  result: {
    disclaimer?: string;
    productLabel?: string;
    rulesetVersion?: string;
    applicableCount?: number;
    uncertainCount?: number;
    notApplicableCount?: number;
    applicableRuleCodes?: string[];
    uncertainRuleCodes?: string[];
    riskSuggestion?: {
      suggestedRiskClass?: string;
      currentRiskClass?: string | null;
      autoApplied?: boolean;
      requiresHumanConfirm?: boolean;
      rationale?: string;
      note?: string;
    };
    [key: string]: unknown;
  };
  status: string;
  rulesetVersion: string;
  createdBy: string | null;
  createdAt: string;
  obligations: DeterminationObligationItem[];
  disclaimer: string;
}

export interface SystemViewModel {
  id: string;
  name: string;
  owner: string;
  purpose: string;
  riskClass: RiskClass;
  riskBasis: string;
  evidenceCoverage: number;
  evalScore: number;
  dataContractStatus: DataContractStatus;
  releaseDecision: "Pass" | "Review" | "Blocked";
  openGaps: string[];
}

export type WorkflowStatus = "OPEN" | "APPROVED" | "REJECTED" | "SUPERSEDED";
export type WorkflowTrigger =
  | "SYSTEM_CREATED"
  | "EVAL_REGRESSION"
  | "CONTRACT_BREACH"
  | "RISK_RECLASSIFIED"
  | "HIGH_RISK_PASS";
export type StageStatus = "PENDING" | "APPROVED" | "REJECTED" | "OVERRIDDEN" | "SKIPPED";
export type StageType = "ENG_LEAD_REVIEW" | "COMPLIANCE_REVIEW" | "LEGAL_SIGNOFF";

export interface ApprovalStage {
  id: string;
  workflowId: string;
  stageOrder: number;
  stageType: StageType;
  requiredRole: string;
  assignedReviewerId: string | null;
  status: StageStatus;
  actorId: string | null;
  rationale: string | null;
  oversightEvidence: string | null;
  actedAt: string | null;
  notificationSentAt: string | null;
  createdAt: string;
}

export interface ApprovalWorkflow {
  id: string;
  systemId: string;
  trigger: WorkflowTrigger;
  status: WorkflowStatus;
  stages: ApprovalStage[];
  openedAt: string;
  closedAt: string | null;
  createdAt: string;
}

export interface WorkflowNotification {
  id: string;
  workflowId: string;
  stageId: string | null;
  recipientId: string | null;
  eventType: string;
  message: string;
  readAt: string | null;
  createdAt: string;
}
