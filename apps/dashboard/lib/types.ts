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
