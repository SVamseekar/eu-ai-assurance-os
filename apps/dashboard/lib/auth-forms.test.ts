import assert from "node:assert/strict";
import { describe, it } from "node:test";
import { passwordProblem } from "./auth-forms";

describe("passwordProblem", () => {
  it("requires 12 to 128 characters", () => {
    assert.match(passwordProblem("short", "short") ?? "", /12/);
    assert.match(passwordProblem("x".repeat(129), "x".repeat(129)) ?? "", /128/);
    assert.equal(passwordProblem("x".repeat(12), "x".repeat(12)), null);
  });
  it("requires the confirmation to match", () => {
    assert.match(passwordProblem("long-enough-password", "long-enough-passwore") ?? "", /match/);
  });
});
