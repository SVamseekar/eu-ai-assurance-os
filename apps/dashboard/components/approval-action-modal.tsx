"use client";

import { useState } from "react";
import { Modal } from "./ui/modal";
import { Button } from "./ui/button";
import { Textarea } from "./ui/textarea";
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
    buttonVariant: "default" as const,
    rationaleRequired: false,
    placeholder: "Optional notes for the audit record...",
  },
  reject: {
    title: "Reject Stage",
    buttonLabel: "Reject",
    buttonVariant: "destructive" as const,
    rationaleRequired: true,
    placeholder: "Describe what must be resolved before this system can proceed...",
  },
  override: {
    title: "Override Stage",
    buttonLabel: "Override",
    buttonVariant: "destructive" as const,
    rationaleRequired: true,
    placeholder: "Explain the business or regulatory justification for this override...",
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
    <Modal
      isOpen
      onClose={onClose}
      title={config.title}
      description={`Stage: ${STAGE_LABELS[stage.stageType]}`}
    >
      <div className="space-y-4">
        <div className="space-y-1.5">
          <label className="text-xs font-medium text-foreground">
            Rationale{config.rationaleRequired ? " (required)" : " (optional)"}
          </label>
          <Textarea
            rows={4}
            placeholder={config.placeholder}
            value={rationale}
            onChange={(e) => setRationale(e.target.value)}
          />
        </div>

        <div className="flex gap-2 justify-end">
          <Button variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button
            variant={config.buttonVariant}
            disabled={!canSubmit}
            onClick={() => onConfirm(rationale)}
          >
            {config.buttonLabel}
          </Button>
        </div>
      </div>
    </Modal>
  );
}
