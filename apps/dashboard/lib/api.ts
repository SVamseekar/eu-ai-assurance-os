import type {
  AiSystem,
  ApprovalWorkflow,
  AuditEvent,
  CertificationReadiness,
  Control,
  ControlStatus,
  DataContract,
  DeterminationQuestionnaire,
  DeterminationRun,
  DriftEvent,
  EvalRun,
  EvalRunOperationsView,
  EvidenceDocument,
  EvidencePack,
  EvidenceQueryResponse,
  RegItem,
  RegMonitorFeed,
  ReleaseGateResponse,
  SystemControl,
  WorkflowNotification,
} from "./types";

const BASE = "/api/proxy";

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers);
  if (init?.body !== undefined && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  const res = await fetch(`${BASE}${path}`, {
    ...init,
    headers,
  });
  if (res.status === 401 && typeof window !== "undefined") {
    window.location.href = "/login";
  }
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
  return res.status === 204 ? (null as T) : res.json();
}

function triggerBrowserDownload(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}

export const api = {
  systems: {
    list: () => request<AiSystem[]>("/systems"),
    get: (id: string) => request<AiSystem>(`/systems/${id}`),
    releaseGate: (id: string) => request<ReleaseGateResponse>(`/systems/${id}/release-gate`),
    controls: (id: string) => request<SystemControl[]>(`/systems/${id}/controls`),
    updateControl: (systemId: string, controlId: string, status: ControlStatus, notes?: string) =>
      request<SystemControl>(`/systems/${systemId}/controls/${controlId}`, {
        method: "PUT",
        body: JSON.stringify({ status, notes }),
      }),
    /** Primary sealed JSON evidence pack (authenticated proxy). */
    evidencePack: (id: string) => request<EvidencePack>(`/systems/${id}/evidence-pack`),
    /**
     * Phase 6 PDF export of the sealed pack. Returns contentSha256 from response header.
     */
    evidencePackPdf: async (id: string): Promise<{ contentSha256: string; filename: string }> => {
      const res = await fetch(`${BASE}/systems/${id}/evidence-pack.pdf`);
      if (res.status === 401 && typeof window !== "undefined") {
        window.location.href = "/login";
      }
      if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
      const contentSha256 = res.headers.get("X-Content-Sha256") ?? "";
      const disposition = res.headers.get("Content-Disposition") ?? "";
      const match = /filename="?([^";]+)"?/i.exec(disposition);
      const filename =
        match?.[1] ?? `evidence-pack-${id}-${new Date().toISOString().slice(0, 10)}.pdf`;
      const blob = await res.blob();
      triggerBrowserDownload(blob, filename);
      return { contentSha256, filename };
    },
    /** Certification readiness score + gaps (not legal certification). */
    certificationReadiness: (id: string) =>
      request<CertificationReadiness>(`/systems/${id}/certification-readiness`),
    certificationReadinessExport: async (
      id: string,
      format: "json" | "pdf" = "json"
    ): Promise<{ readinessStatus: string; filename: string }> => {
      const res = await fetch(`${BASE}/systems/${id}/certification-readiness/export`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ format }),
      });
      if (res.status === 401 && typeof window !== "undefined") {
        window.location.href = "/login";
      }
      if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
      const readinessStatus = res.headers.get("X-Readiness-Status") ?? "";
      const disposition = res.headers.get("Content-Disposition") ?? "";
      const match = /filename="?([^";]+)"?/i.exec(disposition);
      const filename =
        match?.[1] ??
        `certification-readiness-${id}.${format === "pdf" ? "pdf" : "json"}`;
      const blob = await res.blob();
      triggerBrowserDownload(blob, filename);
      return { readinessStatus, filename };
    },
  },
  controls: {
    catalog: () => request<Control[]>("/controls"),
  },
  workflows: {
    open: () => request<ApprovalWorkflow[]>("/workflows/open"),
    mine: () => request<ApprovalWorkflow[]>("/workflows/mine"),
    list: (systemId: string) =>
      request<ApprovalWorkflow[]>(`/systems/${systemId}/workflows`),
    active: (systemId: string) =>
      request<ApprovalWorkflow>(`/systems/${systemId}/workflows/active`),
    approve: (
      systemId: string,
      workflowId: string,
      stageId: string,
      rationale?: string,
      oversightEvidence?: string
    ) =>
      request<ApprovalWorkflow>(
        `/systems/${systemId}/workflows/${workflowId}/stages/${stageId}/approve`,
        { method: "POST", body: JSON.stringify({ rationale, oversightEvidence }) }
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
  notifications: {
    mine: () => request<WorkflowNotification[]>("/workflow-notifications/mine"),
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
  determination: {
    questionnaire: () => request<DeterminationQuestionnaire>("/determination/questionnaire"),
    createRun: (systemId: string, answers: Record<string, unknown>) =>
      request<DeterminationRun>(`/systems/${systemId}/determination/runs`, {
        method: "POST",
        body: JSON.stringify({ answers }),
      }),
    getRun: (systemId: string, runId: string) =>
      request<DeterminationRun>(`/systems/${systemId}/determination/runs/${runId}`),
    listRuns: (systemId: string) =>
      request<DeterminationRun[]>(`/systems/${systemId}/determination/runs`),
  },
  regMonitor: {
    items: (params?: { since?: string; reviewed?: boolean }) => {
      const q = new URLSearchParams();
      if (params?.since) q.set("since", params.since);
      if (params?.reviewed !== undefined) q.set("reviewed", String(params.reviewed));
      const suffix = q.toString() ? `?${q.toString()}` : "";
      return request<RegMonitorFeed>(`/reg-monitor/items${suffix}`);
    },
    relevant: (systemId: string) =>
      request<RegMonitorFeed>(`/systems/${systemId}/reg-monitor/relevant`),
    markReviewed: (itemId: string, notes?: string) =>
      request<RegItem>(`/reg-monitor/items/${itemId}/review`, {
        method: "POST",
        body: JSON.stringify({ notes }),
      }),
  },
};
