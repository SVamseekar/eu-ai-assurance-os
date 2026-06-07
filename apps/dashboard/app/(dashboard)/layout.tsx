"use client";

import { Sidebar } from "@/components/sidebar";
import { Header } from "@/components/header";
import { usePathname } from "next/navigation";
import { normaliseDecision } from "@/lib/utils";
import { useDashboard } from "@/context/dashboard-context";
import { SystemDetailsSheet, ContractDetailsSheet } from "@/components/details-sheets";
import { MOCK_AUDIT_EVENTS, MOCK_DRIFT_EVENTS } from "@/lib/mock-data";

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
    setSelectedContract
  } = useDashboard();

  const blockedCount = allSystems.filter(
    (s) => normaliseDecision(s.releaseDecision) === "Blocked"
  ).length;

  const meta = PAGE_META[pathname] ?? PAGE_META["/command"];

  function handleExportPack() {
    const pack = {
      product: "EU AI Assurance OS",
      generatedAt: new Date().toISOString(),
      systems: allSystems,
    };
    downloadJson(`eu-ai-assurance-evidence-${new Date().toISOString().slice(0, 10)}.json`, pack);
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
          onExportPack={handleExportPack}
          onRunControls={handleRunControls}
        />
        {children}
      </main>

      {/* Slide-over details drawers */}
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
    </div>
  );
}
