import assert from "node:assert/strict";
import { afterEach, describe, it } from "node:test";
import { NextRequest } from "next/server";
import { POST } from "./route";

const realFetch = globalThis.fetch;
afterEach(() => {
  globalThis.fetch = realFetch;
});

function request(body: unknown, query = "") {
  return new NextRequest(`http://dashboard.test/api/auth/verify-email${query}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
}

describe("POST /api/auth/verify-email", () => {
  it("sets session cookies and hides the tokens from the browser on success", async () => {
    let upstreamBody = "";
    globalThis.fetch = async (_url, init) => {
      upstreamBody = String(init?.body);
      return new Response(JSON.stringify({ accessToken: "acc", refreshToken: "ref" }), { status: 200 });
    };
    const res = await POST(request({ token: "tok", password: "correct-horse-battery" }));
    assert.equal(res.status, 200);
    assert.deepEqual(await res.json(), { ok: true });
    assert.deepEqual(JSON.parse(upstreamBody), { token: "tok", password: "correct-horse-battery" });
    assert.equal(res.cookies.get("session_access")?.value, "acc");
    assert.equal(res.cookies.get("session_refresh")?.value, "ref");
  });

  it("passes a rejected link through as 410 without cookies", async () => {
    globalThis.fetch = async () => new Response("{}", { status: 410 });
    const res = await POST(request({ token: "old", password: "correct-horse-battery" }));
    assert.equal(res.status, 410);
    assert.equal(res.cookies.get("session_access"), undefined);
  });

  it("resends through the same route with ?resend=1 and always answers 202", async () => {
    let url = "";
    globalThis.fetch = async (u) => {
      url = String(u);
      return new Response("{}", { status: 202 });
    };
    const res = await POST(request({ email: "a@b.example" }, "?resend=1"));
    assert.equal(res.status, 202);
    assert.match(url, /\/auth\/verify-email\/resend$/);
  });
});
