import type {
  AiSystem,
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

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    headers: { "Content-Type": "application/json", ...init?.headers },
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
