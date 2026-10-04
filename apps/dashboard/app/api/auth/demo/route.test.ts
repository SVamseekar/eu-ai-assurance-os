import assert from "node:assert/strict";
import { afterEach, describe, it } from "node:test";
import { NextRequest } from "next/server";
import { POST } from "./route";

const realFetch = globalThis.fetch;
afterEach(() => {
  globalThis.fetch = realFetch;
});

const request = (headers: Record<string, string> = {}) =>
  new NextRequest("http://dashboard.test/api/auth/demo", { method: "POST", headers });

describe("POST /api/auth/demo", () => {
  it("starts a demo session with an access cookie only", async () => {
    globalThis.fetch = async () =>
      new Response(JSON.stringify({ accessToken: "demo-acc", refreshToken: "" }), { status: 200 });
    const res = await POST(request());
    assert.equal(res.status, 200);
    assert.deepEqual(await res.json(), { ok: true, next: "/command" });
    assert.equal(res.cookies.get("session_access")?.value, "demo-acc");
    assert.equal(res.cookies.get("session_refresh")?.value, "");
  });

  it("expires any refresh cookie left over from an earlier real session", async () => {
    globalThis.fetch = async () =>
      new Response(JSON.stringify({ accessToken: "demo-acc", refreshToken: "" }), { status: 200 });
    const res = await POST(request({ cookie: "session_refresh=real-refresh" }));
    const refresh = res.cookies.get("session_refresh");
    assert.equal(refresh?.value, "");
    assert.equal(refresh?.maxAge, 0);
  });

  it("forwards the visitor's IP so the API rate-limits visitors separately", async () => {
    let sent: Record<string, string> = {};
    globalThis.fetch = async (_u, init) => {
      sent = init?.headers as Record<string, string>;
      return new Response(JSON.stringify({ accessToken: "a", refreshToken: "" }), { status: 200 });
    };
    await POST(request({ "cf-connecting-ip": "198.51.100.7" }));
    assert.equal(sent["X-Client-IP"], "198.51.100.7");
  });

  it("reports the demo as unavailable when the API has it switched off", async () => {
    globalThis.fetch = async () => new Response("{}", { status: 404 });
    const res = await POST(request());
    assert.equal(res.status, 404);
    assert.equal(res.cookies.get("session_access"), undefined);
  });

  it("answers 503 when the API is unreachable", async () => {
    globalThis.fetch = async () => {
      throw new TypeError("fetch failed");
    };
    assert.equal((await POST(request())).status, 503);
  });
});
