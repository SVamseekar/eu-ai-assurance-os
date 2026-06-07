"use client";

import { useState } from "react";
import type { ApprovalStage } from "@/lib/types";

interface ApprovalActionModalProps {
  stage: ApprovalStage;
  action: "approve" | "reject" | "override";
  onConfirm: (rationale: string) => void;
  onClose: () => void;
}

const STAGE_LABELS: Record<string, string> = {
  ENG_LEAD_REVIEW: "Engineering Lead Review",
  COMPLIANCE_REVIEW: "Compliance Review",
  LEGAL_SIGNOFF: "Legal Sign-off",
};

const ACTION_CONFIG = {
  approve: {
    title: "Approve Stage",
    buttonLabel: "Approve",
    buttonClass: "bg-emerald-600 hover:bg-emerald-700 text-white",
    rationaleRequired: false,
  },
  reject: {
    title: "Reject Stage",
    buttonLabel: "Reject",
    buttonClass: "bg-red-600 hover:bg-red-700 text-white",
    rationaleRequired: true,
  },
  override: {
    title: "Override Stage",
    buttonLabel: "Override",
    buttonClass: "bg-amber-600 hover:bg-amber-700 text-white",
    rationaleRequired: true,
  },
};

export function ApprovalActionModal({
  stage,
  action,
  onConfirm,
  onClose,
}: ApprovalActionModalProps) {
  const [rationale, setRationale] = useState("");
  const config = ACTION_CONFIG[action];
  const canSubmit = !config.rationaleRequired || rationale.trim().length > 0;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="bg-card border border-border rounded-xl shadow-xl w-full max-w-md p-6 space-y-4">
        <div>
          <h3 className="text-sm font-semibold text-foreground">{config.title}</h3>
          <p className="text-xs text-muted-foreground mt-1">
            Stage: {STAGE_LABELS[stage.stageType]}
          </p>
        </div>

        <div className="space-y-1.5">
          <label className="text-xs font-medium text-foreground">
            Rationale{config.rationaleRequired ? " (required)" : " (optional)"}
          </label>
          <textarea
            className="w-full text-xs border border-border rounded-lg p-2.5 bg-background text-foreground placeholder:text-muted-foreground resize-none focus:outline-none focus:ring-2 focus:ring-ring/50"
            rows={4}
            placeholder={
              action === "override"
                ? "Explain the business or regulatory justification for this override..."
                : action === "reject"
                ? "Describe what must be resolved before this system can proceed..."
                : "Optional notes for the audit record..."
            }
            value={rationale}
            onChange={(e) => setRationale(e.target.value)}
          />
        </div>

        <div className="flex gap-2 justify-end">
          <button
            onClick={onClose}
            className="text-xs px-3 py-1.5 rounded-lg border border-border text-muted-foreground hover:bg-muted/50 transition-colors"
          >
            Cancel
          </button>
          <button
            onClick={() => onConfirm(rationale)}
            disabled={!canSubmit}
            className={`text-xs px-3 py-1.5 rounded-lg font-medium transition-colors disabled:opacity-40 disabled:cursor-not-allowed ${config.buttonClass}`}
          >
            {config.buttonLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
