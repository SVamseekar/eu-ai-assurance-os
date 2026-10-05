import assert from "node:assert/strict";
import { describe, it } from "node:test";

import { FRAMEWORKS, frameworkFlag } from "./frameworks";

describe("frameworks", () => {
  it("lists every framework from the launch spec", () => {
    const names = FRAMEWORKS.map((f) => f.short);
    for (const n of ["EU AI Act", "NIST AI RMF", "ISO/IEC 42001", "Texas TRAIGA", "Colorado SB 26-189", "AU Privacy Act ADM", "NZ Biometric Code"]) {
      assert.ok(names.includes(n), n);
    }
  });

  it("never marks a crosswalk live before its pricing feature ships", () => {
    const byShort = Object.fromEntries(FRAMEWORKS.map((f) => [f.short, f.status]));
    assert.equal(byShort["NIST AI RMF"] === "live", frameworkFlag("NIST AI RMF"));
    assert.equal(byShort["Texas TRAIGA"] === "live", frameworkFlag("NIST AI RMF"));
    assert.equal(byShort["ISO/IEC 42001"] === "live", frameworkFlag("ISO/IEC 42001"));
    assert.equal(byShort["AU Privacy Act ADM"] === "live", frameworkFlag("AU automated-decision"));
  });

  it("keeps packs that are not built as coming soon", () => {
    const byShort = Object.fromEntries(FRAMEWORKS.map((f) => [f.short, f.status]));
    for (const n of ["Colorado SB 26-189", "AU Guidance for AI Adoption", "NZ Biometric Code"]) {
      assert.equal(byShort[n], "coming-soon", n);
    }
  });
});
