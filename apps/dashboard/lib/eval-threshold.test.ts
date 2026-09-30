import assert from "node:assert/strict";
import { describe, it } from "node:test";
import { toApiThreshold } from "./eval-threshold";

describe("toApiThreshold", () => {
  it("converts the UI percent to the API fraction", () => {
    assert.equal(toApiThreshold(85), 0.85);
    assert.equal(toApiThreshold(100), 1);
  });
  it("clamps out-of-range values", () => {
    assert.equal(toApiThreshold(140), 1);
    assert.equal(toApiThreshold(-3), 0);
  });
});
