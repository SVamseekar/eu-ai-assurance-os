"use client";

import { useState } from "react";
import { Sidebar } from "@/components/sidebar";
import { Header } from "@/components/header";
import { usePathname } from "next/navigation";
import { normaliseDecision } from "@/lib/utils";
import { useDashboard } from "@/context/dashboard-context";
import { SystemDetailsSheet, ContractDetailsSheet } from "@/components/details-sheets";
import { MOCK_AUDIT_EVENTS, MOCK_DRIFT_EVENTS } from "@/lib/mock-data";
import { api } from "@/lib/api";
import { Modal } from "@/components/ui/modal";
import { Button } from "@/components/ui/button";

const PAGE_META: Record<string, { title: string; subtitle: string }> = {
  "/command": {
    title: "Command Centre",
    subtitle: "Live compliance posture, release gate status, and risk overview across all AI systems.",
  },
  "/systems": {
    title: "AI System Registry",
    subtitle: "Inventory of providers, deployers, risk classifications, and EU Act obligations.",
  },
  "/approvals": {
    title: "Approval Workflows",
    subtitle: "Review and sign off AI system releases. Staged approvals for engineering, compliance, and legal.",
  },
  "/evidence": {
    title: "Compliance Evidence",
    subtitle: "Upload and query policies, DPIAs, model cards, and vendor documentation with citations.",
  },
  "/evals": {
    title: "Eval Gates",
    subtitle: "LLM-as-judge evaluation runs with faithfulness, safety refusal, and latency guardrails.",
  },
  "/contracts": {
    title: "Data Contracts",
    subtitle: "Schema drift monitoring and data lineage across all AI system inputs.",
  },
  "/audit": {
    title: "Audit Ledger",
    subtitle: "Append-only record of evidence checks, approvals, overrides, and release decisions.",
  },
  "/reg-monitor": {
    title: "Regulatory Change Monitor",
    subtitle:
      "Near-real-time polled assistive feed with UNCERTAIN impact hints. Not an official legal bulletin.",
  },
  "/readiness": {
    title: "Certification Readiness",
    subtitle:
      "Weighted readiness score and gaps toward conformity documentation — not legal certification.",
  },
};

function downloadJson(filename: string, payload: unknown) {
  const blob = new Blob([JSON.stringify(payload, null, 2)], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const {
    allSystems,
    selectedSystem,
    setSelectedSystem,
    selectedContract,
    setSelectedContract,
  } = useDashboard();

  const [exportBusy, setExportBusy] = useState(false);
  const [sealModal, setSealModal] = useState<{
    format: "JSON" | "PDF";
    contentSha256: string;
    systemName: string;
    filename?: string;
  } | null>(null);
  const [exportError, setExportError] = useState<string | null>(null);

  const blockedCount = allSystems.filter(
    (s) => normaliseDecision(s.releaseDecision) === "Blocked"
  ).length;

  const meta = PAGE_META[pathname] ?? PAGE_META["/command"];

  function resolveExportTarget() {
    if (selectedSystem) return selectedSystem;
    if (allSystems.length > 0) return allSystems[0];
    return null;
  }

  async function handleExportJson() {
    const target = resolveExportTarget();
    if (!target) {
      setExportError("Register an AI system before exporting an evidence pack.");
      return;
    }
    setExportBusy(true);
    setExportError(null);
    try {
      const pack = await api.systems.evidencePack(target.id);
      const date = pack.generatedAt?.slice(0, 10) ?? new Date().toISOString().slice(0, 10);
      downloadJson(`evidence-pack-${target.id}-${date}.json`, pack);
      setSealModal({
        format: "JSON",
        contentSha256: pack.contentSha256,
        systemName: target.name,
      });
    } catch (err) {
      setExportError(err instanceof Error ? err.message : "JSON export failed");
    } finally {
      setExportBusy(false);
    }
  }

  async function handleExportPdf() {
    const target = resolveExportTarget();
    if (!target) {
      setExportError("Register an AI system before exporting an evidence pack.");
      return;
    }
    setExportBusy(true);
    setExportError(null);
    try {
      const result = await api.systems.evidencePackPdf(target.id);
      setSealModal({
        format: "PDF",
        contentSha256: result.contentSha256,
        systemName: target.name,
        filename: result.filename,
      });
    } catch (err) {
      setExportError(err instanceof Error ? err.message : "PDF export failed");
    } finally {
      setExportBusy(false);
    }
  }

  function handleRunControls() {
    window.location.reload();
  }

  return (
    <div className="min-h-screen bg-background">
      <Sidebar blockedCount={blockedCount} />
      <main className="ml-56 px-8 py-7">
        <Header
          title={meta.title}
          subtitle={meta.subtitle}
          onExportJson={handleExportJson}
          onExportPdf={handleExportPdf}
          onRunControls={handleRunControls}
          exportBusy={exportBusy}
        />
        {children}
      </main>

      <SystemDetailsSheet
        system={selectedSystem}
        isOpen={selectedSystem !== null}
        onClose={() => setSelectedSystem(null)}
        auditEvents={MOCK_AUDIT_EVENTS}
      />

      <ContractDetailsSheet
        contract={selectedContract}
        isOpen={selectedContract !== null}
        onClose={() => setSelectedContract(null)}
        driftEvents={MOCK_DRIFT_EVENTS}
        systems={allSystems}
      />

      <Modal
        isOpen={sealModal !== null}
        onClose={() => setSealModal(null)}
        title={`Evidence pack exported (${sealModal?.format ?? ""})`}
        description={
          sealModal
            ? `Sealed pack for ${sealModal.systemName}. JSON is the primary machine-readable format; PDF is a Phase 6 human-readable export.`
            : undefined
        }
      >
        <div className="space-y-3">
          <div>
            <p className="text-[10px] uppercase font-semibold text-muted-foreground tracking-wider mb-1">
              contentSha256
            </p>
            <code className="block text-[11px] break-all bg-muted/40 border border-border rounded-lg p-3 font-mono leading-relaxed">
              {sealModal?.contentSha256 || "(not returned)"}
            </code>
          </div>
          {sealModal?.filename && (
            <p className="text-xs text-muted-foreground">
              File: <span className="font-medium text-foreground">{sealModal.filename}</span>
            </p>
          )}
          <div className="flex justify-end pt-1">
            <Button size="sm" onClick={() => setSealModal(null)}>
              Close
            </Button>
          </div>
        </div>
      </Modal>

      <Modal
        isOpen={exportError !== null}
        onClose={() => setExportError(null)}
        title="Export failed"
        description="Could not download the evidence pack via the authenticated API proxy."
      >
        <div className="space-y-3">
          <p className="text-xs text-muted-foreground leading-relaxed">{exportError}</p>
          <div className="flex justify-end">
            <Button size="sm" variant="outline" onClick={() => setExportError(null)}>
              Dismiss
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
