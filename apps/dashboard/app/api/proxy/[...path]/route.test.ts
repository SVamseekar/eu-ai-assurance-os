import assert from "node:assert/strict";
import { afterEach, describe, it } from "node:test";
import { NextRequest } from "next/server";
import { POST } from "./route";

const realFetch = globalThis.fetch;
afterEach(() => {
  globalThis.fetch = realFetch;
});

describe("API proxy", () => {
  it("forwards the visitor's IP so per-client limits apply to the visitor, not to this server", async () => {
    let sent: Record<string, string> = {};
    globalThis.fetch = async (_url, init) => {
      sent = init?.headers as Record<string, string>;
      return new Response("{}", { status: 200 });
    };
    const request = new NextRequest("http://dashboard.test/api/proxy/evidence/query", {
      method: "POST",
      headers: { cookie: "session_access=tok", "cf-connecting-ip": "198.51.100.23", "Content-Type": "application/json" },
      body: "{}",
    });
    const res = await POST(request, { params: Promise.resolve({ path: ["evidence", "query"] }) });
    assert.equal(res.status, 200);
    assert.equal(sent["X-Client-IP"], "198.51.100.23");
    assert.equal(sent["Authorization"], "Bearer tok");
  });
});
