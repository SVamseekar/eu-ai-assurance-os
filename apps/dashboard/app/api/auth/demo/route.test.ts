import assert from "node:assert/strict";
import { afterEach, describe, it } from "node:test";
import { NextRequest } from "next/server";
import { POST } from "./route";

const realFetch = globalThis.fetch;
afterEach(() => {
  globalThis.fetch = realFetch;
});

const request = () => new NextRequest("http://dashboard.test/api/auth/demo", { method: "POST" });

describe("POST /api/auth/demo", () => {
  it("starts a demo session with an access cookie only", async () => {
    globalThis.fetch = async () =>
      new Response(JSON.stringify({ accessToken: "demo-acc", refreshToken: "" }), { status: 200 });
    const res = await POST(request());
    assert.equal(res.status, 200);
    assert.deepEqual(await res.json(), { ok: true, next: "/command" });
    assert.equal(res.cookies.get("session_access")?.value, "demo-acc");
    assert.equal(res.cookies.get("session_refresh"), undefined);
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
