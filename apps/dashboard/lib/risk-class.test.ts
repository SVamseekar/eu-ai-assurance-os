import assert from "node:assert/strict";
import { describe, it } from "node:test";
import { suggestRiskClass, toApiRiskClass } from "./risk-class";

describe("risk class helpers", () => {
  it("maps display values to API enums", () => {
    assert.equal(toApiRiskClass("high"), "HIGH");
    assert.equal(toApiRiskClass("limited"), "LIMITED");
  });

  it("suggests HIGH for Annex III style answers", () => {
    assert.equal(suggestRiskClass({ q_biometrics: false, q_essential: true, q_hr: false, q_interaction: true }), "high");
    assert.equal(suggestRiskClass({ q_biometrics: false, q_essential: false, q_hr: true, q_interaction: false }), "high");
  });

  it("suggests LIMITED for Article 50 interaction only, MINIMAL otherwise", () => {
    assert.equal(suggestRiskClass({ q_biometrics: false, q_essential: false, q_hr: false, q_interaction: true }), "limited");
    assert.equal(suggestRiskClass({ q_biometrics: false, q_essential: false, q_hr: false, q_interaction: false }), "minimal");
  });
});
