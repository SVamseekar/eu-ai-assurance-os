import assert from "node:assert/strict";
import { readdirSync } from "node:fs";
import { join } from "node:path";
import { describe, it } from "node:test";

import {
  ANALYTICS_EXCLUDED_PREFIXES,
  analyticsLocation,
  gaCookieNames,
  isAnalyticsExcluded,
  parseConsent,
} from "./analytics-consent";

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

  it("keeps GA off sign-in, token links and the signed-in workspace", () => {
    for (const path of ["/reset-password", "/verify-email", "/invite", "/login", "/systems/abc", "/settings"]) {
      assert.ok(isAnalyticsExcluded(path), path);
    }
    for (const path of ["/", "/pricing", "/privacy", "/blog/some-post", "/systemsx"]) {
      assert.ok(!isAnalyticsExcluded(path), path);
    }
  });

  it("excludes every route under app/(dashboard)", () => {
    const dashboard = join(__dirname, "..", "app", "(dashboard)");
    const routes = readdirSync(dashboard, { withFileTypes: true }).filter((e) => e.isDirectory());
    for (const route of routes) {
      assert.ok(
        (ANALYTICS_EXCLUDED_PREFIXES as readonly string[]).includes(`/${route.name}`),
        `/${route.name} is a signed-in route; add it to ANALYTICS_EXCLUDED_PREFIXES`,
      );
    }
  });

  it("sends GA the path without query string or fragment", () => {
    assert.equal(analyticsLocation("https://example.com", "/pricing"), "https://example.com/pricing");
  });
});
