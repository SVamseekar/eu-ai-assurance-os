import assert from "node:assert/strict";
import { describe, it } from "node:test";

import { relevantDeadlines, toAnswers, type CheckResult } from "./ai-act-check";

function result(cls: CheckResult["riskSuggestion"]["suggestedRiskClass"], codes: string[] = []): CheckResult {
  return {
    obligations: codes.map((ruleCode) => ({
      ruleCode,
      title: ruleCode,
      applicability: "APPLICABLE",
      basis: "",
      legalRefs: null,
      severity: null,
    })),
    riskSuggestion: { suggestedRiskClass: cls, rationale: "" },
    rulesetVersion: "v2",
    disclaimer: "",
  };
}

describe("ai-act-check", () => {
  it("turns yes/no strings into booleans and drops blanks", () => {
    assert.deepEqual(toAnswers({ a: "true", b: "false", c: "unknown", d: "" , e: "finance" }), {
      a: true,
      b: false,
      c: "unknown",
      e: "finance",
    });
  });

  it("maps high-risk results to the Annex III date", () => {
    assert.deepEqual(relevantDeadlines(result("HIGH")).map((d) => d.id), ["eu-annex-iii"]);
  });

  it("adds the Article 50 dates for transparency duties", () => {
    assert.deepEqual(relevantDeadlines(result("LIMITED")).map((d) => d.id), ["eu-art50", "eu-art50-2"]);
    assert.deepEqual(
      relevantDeadlines(result("HIGH", ["ART50_SYNTHETIC_CONTENT"])).map((d) => d.id),
      ["eu-art50", "eu-art50-2", "eu-annex-iii"],
    );
    assert.deepEqual(relevantDeadlines(result("MINIMAL")), []);
  });
});
