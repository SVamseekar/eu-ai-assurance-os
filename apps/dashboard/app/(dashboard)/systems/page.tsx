"use client";

import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { SystemCard } from "@/components/system-card";
import { ReleaseGateTable } from "@/components/release-gate-table";
import { useDashboard } from "@/context/dashboard-context";
import { RegisterSystemModal } from "@/components/register-system-modal";
import { Plus } from "lucide-react";

export default function SystemsPage() {
  const { allSystems, registerSystem } = useDashboard();
  const [isRegisterOpen, setIsRegisterOpen] = useState(false);

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-3 gap-4">
        {/* Dash-border Register Card */}
        <div
          onClick={() => setIsRegisterOpen(true)}
          className="rounded-xl border border-dashed border-border p-4 flex flex-col items-center justify-center text-center cursor-pointer hover:bg-muted/40 hover:border-primary/40 hover:shadow-xs transition-all min-h-36 group bg-muted/10"
        >
          <div className="w-9 h-9 rounded-full bg-primary/15 group-hover:bg-primary/25 flex items-center justify-center transition-colors shrink-0">
            <Plus className="w-4 h-4 text-primary" />
          </div>
          <span className="text-xs font-semibold mt-2.5 text-foreground leading-none">Register System</span>
          <span className="text-[10px] text-muted-foreground mt-1.5 leading-normal max-w-40">
            Run EU AI Act risk questionnaire & obligation mapping
          </span>
        </div>

        {allSystems.map((system) => (
          <SystemCard key={system.id} system={system} />
        ))}
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Release Gate</CardTitle>
          <CardDescription>
            Combined compliance evidence, eval regression, data drift, and human oversight.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <ReleaseGateTable systems={allSystems} />
        </CardContent>
      </Card>

      <RegisterSystemModal
        isOpen={isRegisterOpen}
        onClose={() => setIsRegisterOpen(false)}
        onRegister={registerSystem}
      />
    </div>
  );
}
