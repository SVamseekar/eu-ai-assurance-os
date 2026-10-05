import assert from "node:assert/strict";
import { describe, it } from "node:test";

import { parsePlanIntent } from "./plan-intent";

describe("parsePlanIntent", () => {
  it("reads paid plans and the billing interval from pricing links", () => {
    assert.deepEqual(parsePlanIntent(new URLSearchParams("plan=TEAM&interval=yearly")), { plan: "TEAM", interval: "YEARLY" });
    assert.deepEqual(parsePlanIntent(new URLSearchParams("plan=business")), { plan: "BUSINESS", interval: "MONTHLY" });
  });

  it("ignores free, enterprise and unknown plans", () => {
    for (const q of ["", "plan=FREE", "plan=ENTERPRISE", "plan=evil"]) {
      assert.equal(parsePlanIntent(new URLSearchParams(q)), null, q);
    }
  });
});
