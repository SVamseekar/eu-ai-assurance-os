import type {
  AiSystem,
  ApprovalWorkflow,
  AuditEvent,
  DataContract,
  DriftEvent,
  EvalRun,
  EvalRunOperationsView,
  EvidenceDocument,
  EvidenceQueryResponse,
  ReleaseGateResponse,
} from "./types";

const BASE = "/api/v1";

export const apiHeaders = {
  tenantId: "tenant-premium",
  actorId: "actor-priya",
};

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers);
  headers.set("Content-Type", "application/json");

  if (typeof window !== "undefined") {
    const tId = localStorage.getItem("eu-ai-tenant-id") || apiHeaders.tenantId;
    const aId = localStorage.getItem("eu-ai-actor-id") || apiHeaders.actorId;
    headers.set("X-Tenant-Id", tId);
    headers.set("X-Actor-Id", aId);
  }

  const res = await fetch(`${BASE}${path}`, {
    headers,
    ...init,
  });
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
  return res.status === 204 ? (null as T) : res.json();
}

export const api = {
  systems: {
    list: () => request<AiSystem[]>("/systems"),
    get: (id: string) => request<AiSystem>(`/systems/${id}`),
    releaseGate: (id: string) => request<ReleaseGateResponse>(`/systems/${id}/release-gate`),
  },
  workflows: {
    list: (systemId: string) =>
      request<ApprovalWorkflow[]>(`/systems/${systemId}/workflows`),
    active: (systemId: string) =>
      request<ApprovalWorkflow>(`/systems/${systemId}/workflows/active`),
    approve: (systemId: string, workflowId: string, stageId: string, rationale?: string) =>
      request<ApprovalWorkflow>(
        `/systems/${systemId}/workflows/${workflowId}/stages/${stageId}/approve`,
        { method: "POST", body: JSON.stringify({ rationale }) }
      ),
    reject: (systemId: string, workflowId: string, stageId: string, rationale: string) =>
      request<ApprovalWorkflow>(
        `/systems/${systemId}/workflows/${workflowId}/stages/${stageId}/reject`,
        { method: "POST", body: JSON.stringify({ rationale }) }
      ),
    override: (systemId: string, workflowId: string, stageId: string, rationale: string) =>
      request<ApprovalWorkflow>(
        `/systems/${systemId}/workflows/${workflowId}/stages/${stageId}/override`,
        { method: "POST", body: JSON.stringify({ rationale }) }
      ),
  },
  evidence: {
    documents: (systemId: string) =>
      request<EvidenceDocument[]>(`/evidence/systems/${systemId}/documents`),
    query: (systemId: string, question: string) =>
      request<EvidenceQueryResponse>("/evidence/query", {
        method: "POST",
        body: JSON.stringify({ systemId, question }),
      }),
    index: (payload: {
      systemId: string;
      type: string;
      title: string;
      sourceUri: string;
      content?: string;
      metadata?: Record<string, string>;
    }) =>
      request<EvidenceDocument>("/evidence/documents", {
        method: "POST",
        body: JSON.stringify(payload),
      }),
  },
  evals: {
    operations: () => request<EvalRunOperationsView>("/eval-runs/operations"),
    get: (id: string) => request<EvalRun>(`/eval-runs/${id}`),
    create: (payload: {
      systemId: string;
      dataset: string;
      modelVersion: string;
      promptVersion: string;
      threshold: number;
    }) =>
      request<{ runId: string; status: string }>("/eval-runs", {
        method: "POST",
        body: JSON.stringify(payload),
      }),
    execute: (id: string) =>
      request<EvalRun>(`/eval-runs/${id}/execute`, { method: "POST" }),
  },
  contracts: {
    list: (systemId?: string) =>
      request<DataContract[]>(
        systemId ? `/data-contracts?systemId=${systemId}` : "/data-contracts"
      ),
    get: (id: string) => request<DataContract>(`/data-contracts/${id}`),
    driftEvents: (id: string) =>
      request<DriftEvent[]>(`/data-contracts/${id}/drift-events`),
  },
  audit: {
    list: (systemId?: string) =>
      request<AuditEvent[]>(
        systemId ? `/audit-events?systemId=${systemId}` : "/audit-events"
      ),
  },
};
