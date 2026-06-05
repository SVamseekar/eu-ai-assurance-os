"use client";

import { Sidebar } from "@/components/sidebar";
import { Header } from "@/components/header";
import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { MOCK_SYSTEMS } from "@/lib/mock-data";
import { normaliseDecision } from "@/lib/utils";

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
  const { data: systems } = useQuery({
    queryKey: ["systems"],
    queryFn: api.systems.list,
    placeholderData: MOCK_SYSTEMS,
  });

  const blockedCount = (systems ?? MOCK_SYSTEMS).filter(
    (s) => normaliseDecision(s.releaseDecision) === "Blocked"
  ).length;

  function handleExportPack() {
    const pack = {
      product: "EU AI Assurance OS",
      generatedAt: new Date().toISOString(),
      systems: systems ?? MOCK_SYSTEMS,
    };
    downloadJson(
      `eu-ai-assurance-evidence-${new Date().toISOString().slice(0, 10)}.json`,
      pack
    );
  }

  function handleRunControls() {
    window.location.reload();
  }

  return (
    <div className="min-h-screen bg-background">
      <Sidebar blockedCount={blockedCount} />
      <main className="ml-64 p-7">
        <Header onExportPack={handleExportPack} onRunControls={handleRunControls} />
        {children}
      </main>
    </div>
  );
}
