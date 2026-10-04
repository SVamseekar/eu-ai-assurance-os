import assert from "node:assert/strict";
import { describe, it } from "node:test";
import { NextResponse } from "next/server";
import { setSessionCookies } from "./session";

describe("setSessionCookies", () => {
  it("sets both cookies for a normal sign-in", () => {
    const res = NextResponse.json({});
    setSessionCookies(res, "acc", "ref");
    assert.equal(res.cookies.get("session_access")?.value, "acc");
    assert.equal(res.cookies.get("session_refresh")?.value, "ref");
  });

  it("sets only the access cookie when there is no refresh token (demo)", () => {
    const res = NextResponse.json({});
    setSessionCookies(res, "acc", "");
    assert.equal(res.cookies.get("session_access")?.value, "acc");
    assert.equal(res.cookies.get("session_refresh"), undefined);
  });
});
