import assert from "node:assert/strict";
import { describe, it } from "node:test";
import { billingBanner, daysLeft, usagePercent } from "./billing-view";
import type { BillingSummary } from "./types";

const NOW = Date.parse("2026-11-10T12:00:00Z");
const base: BillingSummary = {
  plan: "FREE",
  status: "NONE",
  interval: null,
  trialEndsAt: null,
  currentPeriodEnd: null,
  graceUntil: null,
  usage: { systems: 0, editors: 1, gateRunsThisMonth: 0 },
  limits: { gatedSystems: 1, editorSeats: 3, gateRunsPerMonth: 300 },
};

describe("billingBanner", () => {
  it("counts down a trial", () => {
    const b = { ...base, plan: "TRIAL" as const, status: "TRIAL", trialEndsAt: "2026-11-13T12:00:00Z" };
    assert.match(billingBanner(b, NOW)!, /trial ends in 3 days/);
  });
  it("warns about a failed payment with the grace deadline", () => {
    const b = { ...base, plan: "TEAM" as const, status: "ON_HOLD", graceUntil: "2026-11-11T12:00:00Z" };
    assert.match(billingBanner(b, NOW)!, /payment failed.*within 1 day\b/);
  });
  it("says when a cancelled plan ends", () => {
    const b = { ...base, plan: "TEAM" as const, status: "CANCELLED", currentPeriodEnd: "2026-11-20T12:00:00Z" };
    assert.match(billingBanner(b, NOW)!, /cancelled and ends in 10 days/);
  });
  it("stays quiet for healthy paid, free and demo workspaces", () => {
    assert.equal(billingBanner({ ...base, plan: "TEAM", status: "ACTIVE" }, NOW), null);
    assert.equal(billingBanner(base, NOW), null);
    assert.equal(billingBanner({ ...base, plan: "DEMO" }, NOW), null);
  });
});

describe("helpers", () => {
  it("daysLeft never goes negative", () => {
    assert.equal(daysLeft("2026-11-01T00:00:00Z", NOW), 0);
    assert.equal(daysLeft(null, NOW), null);
  });
  it("usagePercent handles unlimited, zero and over-limit", () => {
    assert.equal(usagePercent(5, -1), null);
    assert.equal(usagePercent(2, 4), 50);
    assert.equal(usagePercent(9, 3), 100);
    assert.equal(usagePercent(0, 0), 100);
  });
});
