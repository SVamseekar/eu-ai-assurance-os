"use client";

import { ApprovalActionModal } from "@/components/approval-action-modal";
import { Button } from "@/components/ui/button";
import { api } from "@/lib/api";
import {
  MOCK_MY_WORKFLOWS,
  MOCK_NOTIFICATIONS,
  MOCK_OPEN_WORKFLOWS,
  MOCK_SYSTEMS,
} from "@/lib/mock-data";
import type { ApprovalStage, ApprovalWorkflow } from "@/lib/types";
import { STAGE_LABELS } from "@/lib/workflow-helpers";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Bell, Clock, ShieldCheck } from "lucide-react";
import { useState } from "react";

type ModalState = {
  stage: ApprovalStage;
  action: "approve" | "reject" | "override";
  systemId: string;
  workflowId: string;
} | null;

function nextPendingStage(workflow: ApprovalWorkflow): ApprovalStage | undefined {
  return workflow.stages
    .filter((stage) => stage.status === "PENDING")
    .sort((a, b) => a.stageOrder - b.stageOrder)[0];
}

function openedLabel(openedAt: string): string {
  const openedMs = Date.now() - new Date(openedAt).getTime();
  const openedDays = Math.max(0, Math.floor(openedMs / 86_400_000));
  return openedDays === 0 ? "opened today" : `opened ${openedDays}d ago`;
}

export default function ApprovalsPage() {
  const queryClient = useQueryClient();
  const [modal, setModal] = useState<ModalState>(null);

  const systems = useQuery({
    queryKey: ["systems"],
    queryFn: api.systems.list,
    placeholderData: MOCK_SYSTEMS,
  });
  const mine = useQuery({
    queryKey: ["workflows", "mine"],
    queryFn: api.workflows.mine,
    placeholderData: MOCK_MY_WORKFLOWS,
  });
  const open = useQuery({
    queryKey: ["workflows", "open"],
    queryFn: api.workflows.open,
    placeholderData: MOCK_OPEN_WORKFLOWS,
  });
  const notifications = useQuery({
    queryKey: ["workflow-notifications", "mine"],
    queryFn: api.notifications.mine,
    placeholderData: MOCK_NOTIFICATIONS,
  });

  const systemNames = new Map((systems.data ?? []).map((system) => [system.id, system.name]));
  const myWorkflowIds = new Set((mine.data ?? []).map((workflow) => workflow.id));
  const otherItems = (open.data ?? []).filter((workflow) => !myWorkflowIds.has(workflow.id));

  const action = useMutation({
    mutationFn: async ({
      actionType,
      systemId,
      workflowId,
      stageId,
      rationale,
      oversightEvidence,
    }: {
      actionType: "approve" | "reject" | "override";
      systemId: string;
      workflowId: string;
      stageId: string;
      rationale: string;
      oversightEvidence?: string;
    }) => {
      if (actionType === "approve") {
        return api.workflows.approve(systemId, workflowId, stageId, rationale, oversightEvidence);
      }
      if (actionType === "reject") {
        return api.workflows.reject(systemId, workflowId, stageId, rationale);
      }
      return api.workflows.override(systemId, workflowId, stageId, rationale);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["workflows"] });
      queryClient.invalidateQueries({ queryKey: ["workflow-notifications"] });
      queryClient.invalidateQueries({ queryKey: ["audit"] });
    },
  });

  const isLoading = mine.isLoading || open.isLoading || systems.isLoading;

  return (
    <div className="space-y-6">
      {notifications.data && notifications.data.length > 0 && (
        <section className="border border-border bg-card px-4 py-3">
          <div className="flex items-center gap-2">
            <Bell className="h-4 w-4 text-primary" />
            <h2 className="text-xs font-semibold uppercase tracking-widest text-muted-foreground">
              Notifications
            </h2>
          </div>
          <div className="mt-3 grid gap-2 md:grid-cols-2">
            {notifications.data.slice(0, 4).map((notification) => (
              <div key={notification.id} className="border border-border bg-muted/20 px-3 py-2">
                <p className="text-xs font-medium text-foreground">{notification.message}</p>
                <p className="mt-0.5 text-[10px] text-muted-foreground">
                  {new Date(notification.createdAt).toLocaleString()}
                </p>
              </div>
            ))}
          </div>
        </section>
      )}

      <section>
        <h2 className="mb-3 text-xs font-semibold uppercase tracking-widest text-muted-foreground">
          Awaiting your action
        </h2>
        {isLoading ? (
          <p className="border border-dashed border-border py-6 text-center text-xs text-muted-foreground">
            Loading approval queue...
          </p>
        ) : (mine.data ?? []).length === 0 ? (
          <p className="border border-dashed border-border py-6 text-center text-xs text-muted-foreground">
            No stages require your review right now.
          </p>
        ) : (
          <div className="space-y-2">
            {(mine.data ?? []).map((workflow) => {
              const activeStage = nextPendingStage(workflow);
              if (!activeStage) return null;
              return (
                <div
                  key={workflow.id}
                  className="flex items-center justify-between border border-border bg-card px-4 py-3"
                >
                  <div className="flex items-center gap-3">
                    <ShieldCheck className="h-4 w-4 text-amber-500" />
                    <div>
                      <p className="text-sm font-medium text-foreground">
                        {systemNames.get(workflow.systemId) ?? workflow.systemId}
                      </p>
                      <p className="mt-0.5 flex items-center gap-1 text-[10px] text-muted-foreground">
                        <Clock className="h-3 w-3" />
                        {STAGE_LABELS[activeStage.stageType]} · {openedLabel(workflow.openedAt)}
                      </p>
                    </div>
                  </div>
                  <div className="flex gap-1.5">
                    <Button
                      size="sm"
                      onClick={() =>
                        setModal({
                          stage: activeStage,
                          action: "approve",
                          systemId: workflow.systemId,
                          workflowId: workflow.id,
                        })
                      }
                    >
                      Approve
                    </Button>
                    <Button
                      size="sm"
                      variant="destructive"
                      onClick={() =>
                        setModal({
                          stage: activeStage,
                          action: "reject",
                          systemId: workflow.systemId,
                          workflowId: workflow.id,
                        })
                      }
                    >
                      Reject
                    </Button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </section>

      <section>
        <h2 className="mb-3 text-xs font-semibold uppercase tracking-widest text-muted-foreground">
          In progress
        </h2>
        {otherItems.length === 0 ? (
          <p className="border border-dashed border-border py-6 text-center text-xs text-muted-foreground">
            No other workflows in progress.
          </p>
        ) : (
          <div className="space-y-2">
            {otherItems.map((workflow) => {
              const activeStage = nextPendingStage(workflow);
              return (
                <div
                  key={workflow.id}
                  className="flex items-center justify-between border border-border bg-muted/20 px-4 py-3"
                >
                  <div>
                    <p className="text-sm font-medium text-foreground">
                      {systemNames.get(workflow.systemId) ?? workflow.systemId}
                    </p>
                    {activeStage && (
                      <p className="mt-0.5 text-[10px] text-muted-foreground">
                        {STAGE_LABELS[activeStage.stageType]} · waiting for{" "}
                        {activeStage.requiredRole.replace(/_/g, " ")}
                      </p>
                    )}
                  </div>
                  <span className="text-[10px] font-medium text-muted-foreground">
                    {workflow.trigger.replace(/_/g, " ")}
                  </span>
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
          onConfirm={(rationale, oversightEvidence) => {
            action.mutate({
              actionType: modal.action,
              systemId: modal.systemId,
              workflowId: modal.workflowId,
              stageId: modal.stage.id,
              rationale,
              oversightEvidence,
            });
            setModal(null);
          }}
        />
      )}
    </div>
  );
}
