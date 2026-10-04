import assert from "node:assert/strict";
import { describe, it } from "node:test";
import { PLANS, formatLimit, formatPrice } from "./pricing";

const byCode = (code: string) => PLANS.find((p) => p.code === code)!;

describe("PLANS", () => {
  it("matches the pricing table", () => {
    const free = byCode("FREE");
    assert.deepEqual([free.monthly, free.yearly, free.gatedSystems, free.editors, free.gateRunsPerMonth], [0, 0, 1, 3, 300]);
    const team = byCode("TEAM");
    assert.deepEqual([team.monthly, team.yearly, team.gatedSystems, team.editors, team.gateRunsPerMonth], [79, 790, 3, 10, null]);
    const business = byCode("BUSINESS");
    assert.deepEqual(
      [business.monthly, business.yearly, business.gatedSystems, business.editors, business.gateRunsPerMonth],
      [299, 2990, 15, null, null],
    );
    const enterprise = byCode("ENTERPRISE");
    assert.deepEqual([enterprise.monthly, enterprise.yearly], [null, null]);
  });

  it("lists features that have not shipped as unavailable with a coming-soon label", () => {
    for (const plan of PLANS) {
      for (const feature of plan.features) {
        if (!feature.available) {
          assert.match(feature.label, /\(coming soon\)$/, `${plan.code}: ${feature.label}`);
        }
      }
    }
    const business = byCode("BUSINESS");
    assert.ok(business.features.some((f) => !f.available), "business has at least one unreleased feature at launch");
  });

  it("never describes a paid plan as a compliance guarantee", () => {
    const text = JSON.stringify(PLANS).toLowerCase();
    for (const banned of ["compliant", "certified", "guarantee", "legal advice"]) {
      assert.ok(!text.includes(banned), `found "${banned}"`);
    }
  });
});

describe("formatters", () => {
  it("formats prices and limits", () => {
    assert.equal(formatPrice(79), "$79");
    assert.equal(formatPrice(2990), "$2,990");
    assert.equal(formatPrice(0), "$0");
    assert.equal(formatPrice(null), "Custom");
    assert.equal(formatLimit(null), "Unlimited");
    assert.equal(formatLimit(3), "3");
  });
});
