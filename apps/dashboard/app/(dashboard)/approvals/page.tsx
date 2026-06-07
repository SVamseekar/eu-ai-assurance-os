"use client";

import { useDashboard } from "@/context/dashboard-context";
import { MOCK_WORKFLOWS } from "@/lib/mock-data";
import type { ApprovalStage } from "@/lib/types";
import { Clock } from "lucide-react";
import { useState } from "react";
import { ApprovalActionModal } from "@/components/approval-action-modal";

const STAGE_LABELS: Record<string, string> = {
  ENG_LEAD_REVIEW: "Engineering Lead Review",
  COMPLIANCE_REVIEW: "Compliance Review",
  LEGAL_SIGNOFF: "Legal Sign-off",
};

const ROLE_TO_STAGE: Record<string, string> = {
  "actor-marco": "AI_ENGINEERING_LEAD",
  "actor-priya": "COMPLIANCE_OFFICER",
  "actor-leo": "LEGAL_COUNSEL",
  "actor-sofia": "COMPLIANCE_OFFICER",
};

export default function ApprovalsPage() {
  const { allSystems, activeRole } = useDashboard();
  const actorRole = ROLE_TO_STAGE[activeRole];

  const [modal, setModal] = useState<{
    stage: ApprovalStage;
    action: "approve" | "reject" | "override";
    systemId: string;
    workflowId: string;
  } | null>(null);

  const openWorkflows = allSystems.flatMap((system) => {
    const wfs = MOCK_WORKFLOWS[system.id] ?? [];
    return wfs
      .filter((w) => w.status === "OPEN")
      .map((w) => ({ system, workflow: w }));
  });

  const myItems = openWorkflows.filter(({ workflow }) =>
    workflow.stages.some(
      (s) =>
        s.status === "PENDING" &&
        s.requiredRole === actorRole &&
        !workflow.stages.some(
          (prior) => prior.stageOrder < s.stageOrder && prior.status === "PENDING"
        )
    )
  );

  const otherItems = openWorkflows.filter(
    ({ workflow }) =>
      !myItems.some(({ workflow: w }) => w.id === workflow.id)
  );

  return (
    <div className="space-y-6">
      <section>
        <h2 className="text-xs font-semibold uppercase tracking-widest text-muted-foreground mb-3">
          Awaiting your action
        </h2>
        {myItems.length === 0 ? (
          <p className="text-xs text-muted-foreground py-6 text-center border border-dashed border-border rounded-xl">
            No stages require your review right now.
          </p>
        ) : (
          <div className="space-y-2">
            {myItems.map(({ system, workflow }) => {
              const activeStage = workflow.stages.find(
                (s) =>
                  s.status === "PENDING" &&
                  s.requiredRole === actorRole &&
                  !workflow.stages.some(
                    (p) => p.stageOrder < s.stageOrder && p.status === "PENDING"
                  )
              )!;
              const openedMs = Date.now() - new Date(workflow.openedAt).getTime();
              const openedDays = Math.floor(openedMs / 86_400_000);
              return (
                <div
                  key={workflow.id}
                  className="flex items-center justify-between border border-border rounded-xl px-4 py-3 bg-card"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-2 h-2 rounded-full bg-amber-500 flex-shrink-0" />
                    <div>
                      <p className="text-sm font-medium text-foreground">{system.name}</p>
                      <p className="text-[10px] text-muted-foreground mt-0.5 flex items-center gap-1">
                        <Clock className="w-3 h-3" />
                        {STAGE_LABELS[activeStage.stageType]} · opened {openedDays}d ago
                      </p>
                    </div>
                  </div>
                  <div className="flex gap-1.5">
                    <button
                      onClick={() =>
                        setModal({ stage: activeStage, action: "approve", systemId: system.id, workflowId: workflow.id })
                      }
                      className="text-[10px] px-2.5 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white font-medium"
                    >
                      Approve
                    </button>
                    <button
                      onClick={() =>
                        setModal({ stage: activeStage, action: "reject", systemId: system.id, workflowId: workflow.id })
                      }
                      className="text-[10px] px-2.5 py-1.5 rounded-lg bg-red-600 hover:bg-red-700 text-white font-medium"
                    >
                      Reject
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </section>

      <section>
        <h2 className="text-xs font-semibold uppercase tracking-widest text-muted-foreground mb-3">
          In progress — other stages
        </h2>
        {otherItems.length === 0 ? (
          <p className="text-xs text-muted-foreground py-6 text-center border border-dashed border-border rounded-xl">
            No other workflows in progress.
          </p>
        ) : (
          <div className="space-y-2">
            {otherItems.map(({ system, workflow }) => {
              const activeStage = workflow.stages.find((s) => s.status === "PENDING");
              return (
                <div
                  key={workflow.id}
                  className="flex items-center justify-between border border-border rounded-xl px-4 py-3 bg-muted/20"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-2 h-2 rounded-full bg-muted-foreground/40 flex-shrink-0" />
                    <div>
                      <p className="text-sm font-medium text-foreground">{system.name}</p>
                      {activeStage && (
                        <p className="text-[10px] text-muted-foreground mt-0.5">
                          {STAGE_LABELS[activeStage.stageType]} · waiting for {activeStage.requiredRole.replace(/_/g, " ")}
                        </p>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </section>

      {modal && (
        <ApprovalActionModal
          stage={modal.stage}
          action={modal.action}
          onClose={() => setModal(null)}
          onConfirm={(rationale) => {
            console.log("action", modal.action, modal.workflowId, modal.stage.id, rationale);
            setModal(null);
          }}
        />
      )}
    </div>
  );
}
