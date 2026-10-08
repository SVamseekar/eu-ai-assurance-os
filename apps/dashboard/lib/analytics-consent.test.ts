import assert from "node:assert/strict";
import { describe, it } from "node:test";

import { gaCookieNames, parseConsent } from "./analytics-consent";

describe("analytics consent", () => {
  it("accepts only an explicit choice", () => {
    assert.equal(parseConsent("granted"), "granted");
    assert.equal(parseConsent("denied"), "denied");
    assert.equal(parseConsent(null), null);
    assert.equal(parseConsent("yes"), null);
  });

  it("finds the GA cookies and leaves the rest alone", () => {
    assert.deepEqual(gaCookieNames("session=abc; _ga=GA1.1.1; _ga_VPNF36L7PS=GS1; _gid=x; _gat=1"), [
      "_ga",
      "_ga_VPNF36L7PS",
      "_gid",
    ]);
    assert.deepEqual(gaCookieNames(""), []);
  });
});
