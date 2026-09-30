import type { ApprovalStage, ApprovalWorkflow } from "./types";

export const STAGE_LABELS: Record<string, string> = {
  ENG_LEAD_REVIEW: "Engineering Lead Review",
  COMPLIANCE_REVIEW: "Compliance Review",
  LEGAL_SIGNOFF: "Legal Sign-off",
};

export const ROLE_TO_STAGE: Record<string, string> = {
  "actor-marco": "AI_ENGINEERING_LEAD",
  "actor-priya": "COMPLIANCE_OFFICER",
  "actor-leo": "LEGAL_COUNSEL",
  "actor-sofia": "COMPLIANCE_OFFICER",
};

export function firstPendingStage(workflow: ApprovalWorkflow) {
  const done = new Set(["APPROVED", "OVERRIDDEN"]);
  const stages = [...(workflow.stages ?? [])].sort(
    (a, b) => (a.stageOrder ?? 0) - (b.stageOrder ?? 0),
  );
  return stages.find((s) => !done.has(String(s.status).toUpperCase())) ?? null;
}

export function isActionableStage(stage: ApprovalStage, stages: ApprovalStage[], activeRole: string): boolean {
  if (stage.status !== "PENDING") return false;
  const priorPending = stages.some(
    (s) => s.stageOrder < stage.stageOrder && s.status === "PENDING"
  );
  if (priorPending) return false;
  const actorRole = ROLE_TO_STAGE[activeRole];
  return actorRole === stage.requiredRole || activeRole === "actor-admin";
}
