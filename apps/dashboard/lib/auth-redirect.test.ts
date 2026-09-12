import assert from "node:assert/strict";
import { describe, it } from "node:test";

import { loginRedirectHref, safeNextPath, shouldHardRedirectToLogin } from "./auth-redirect";

describe("shouldHardRedirectToLogin", () => {
  it("does not redirect marketing or auth routes (the production flicker)", () => {
    for (const path of [
      "/",
      "/login",
      "/login/",
      "/request-demo",
      "/privacy",
      "/terms",
      "/refunds",
      "/disclaimer",
    ]) {
      assert.equal(shouldHardRedirectToLogin(path), false, path);
    }
  });

  it("redirects authenticated dashboard routes", () => {
    for (const path of ["/command", "/systems", "/approvals", "/evidence"]) {
      assert.equal(shouldHardRedirectToLogin(path), true, path);
    }
  });

  it("never treats API routes as a UI redirect source", () => {
    assert.equal(shouldHardRedirectToLogin("/api/proxy/systems"), false);
  });
});

describe("loginRedirectHref", () => {
  it("returns null on /login so a 401 cannot reload the sign-in page", () => {
    assert.equal(loginRedirectHref("/login", ""), null);
    assert.equal(loginRedirectHref("/login", "?next=/command"), null);
  });

  it("returns null on the marketing homepage", () => {
    assert.equal(loginRedirectHref("/", ""), null);
  });

  it("sends dashboard routes to /login with a safe next path", () => {
    assert.equal(loginRedirectHref("/command", ""), "/login?next=%2Fcommand");
    assert.equal(
      loginRedirectHref("/systems", "?id=abc"),
      "/login?next=%2Fsystems%3Fid%3Dabc",
    );
  });
});

describe("safeNextPath", () => {
  it("rejects login and protocol-relative values", () => {
    assert.equal(safeNextPath("/login"), "/command");
    assert.equal(safeNextPath("//evil.example"), "/command");
    assert.equal(safeNextPath("/api/auth/login"), "/command");
    assert.equal(safeNextPath("/evidence"), "/evidence");
  });
});
