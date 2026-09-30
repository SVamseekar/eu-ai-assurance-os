import assert from "node:assert/strict";
import { describe, it } from "node:test";
import { firstPendingStage } from "./workflow-helpers";
import type { ApprovalWorkflow } from "./types";

const wf = (statuses: string[]) =>
  ({
    id: "wf-1",
    systemId: "sys-1",
    status: "OPEN",
    stages: statuses.map((status, i) => ({
      id: `st-${i}`,
      status,
      requiredRole: "COMPLIANCE_OFFICER",
      stageOrder: i,
    })),
  }) as unknown as ApprovalWorkflow;

describe("firstPendingStage", () => {
  it("returns the first stage that is not approved or overridden", () => {
    assert.equal(firstPendingStage(wf(["APPROVED", "PENDING", "PENDING"]))?.id, "st-1");
  });
  it("returns null when every stage is complete", () => {
    assert.equal(firstPendingStage(wf(["APPROVED", "OVERRIDDEN"])), null);
  });
});
