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
  createdAt: string;
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
