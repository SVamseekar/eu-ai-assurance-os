"use client";

import { Sidebar } from "@/components/sidebar";
import { Header } from "@/components/header";
import { useQuery } from "@tanstack/react-query";
import { usePathname } from "next/navigation";
import { api } from "@/lib/api";
import { MOCK_SYSTEMS } from "@/lib/mock-data";
import { normaliseDecision } from "@/lib/utils";

const PAGE_META: Record<string, { title: string; subtitle: string }> = {
  "/command": {
    title: "Command Centre",
    subtitle: "Live compliance posture, release gate status, and risk overview across all AI systems.",
  },
  "/systems": {
    title: "AI System Registry",
    subtitle: "Inventory of providers, deployers, risk classifications, and EU Act obligations.",
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
  const { data: systems } = useQuery({
    queryKey: ["systems"],
    queryFn: api.systems.list,
    placeholderData: MOCK_SYSTEMS,
  });

  const blockedCount = (systems ?? MOCK_SYSTEMS).filter(
    (s) => normaliseDecision(s.releaseDecision) === "Blocked"
  ).length;

  const meta = PAGE_META[pathname] ?? PAGE_META["/command"];

  function handleExportPack() {
    const pack = {
      product: "EU AI Assurance OS",
      generatedAt: new Date().toISOString(),
      systems: systems ?? MOCK_SYSTEMS,
    };
    downloadJson(`eu-ai-assurance-evidence-${new Date().toISOString().slice(0, 10)}.json`, pack);
  }

  function handleRunControls() {
    window.location.reload();
  }

  return (
    <div className="min-h-screen bg-background">
      <Sidebar blockedCount={blockedCount} />
      <main className="ml-60 px-8 py-7">
        <Header
          title={meta.title}
          subtitle={meta.subtitle}
          onExportPack={handleExportPack}
          onRunControls={handleRunControls}
        />
        {children}
      </main>
    </div>
  );
}
