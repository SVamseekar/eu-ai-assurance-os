import assert from "node:assert/strict";
import { afterEach, describe, it } from "node:test";
import { NextRequest } from "next/server";
import { POST } from "./route";

const realFetch = globalThis.fetch;
afterEach(() => {
  globalThis.fetch = realFetch;
});

const request = () =>
  new NextRequest("http://dashboard.test/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email: "a@b.example", password: "whatever-long-pw" }),
  });

describe("POST /api/auth/login", () => {
  it("tells the browser when the email is not verified yet", async () => {
    globalThis.fetch = async () =>
      new Response(JSON.stringify({ error: "email_not_verified" }), { status: 403 });
    const res = await POST(request());
    assert.equal(res.status, 403);
    assert.deepEqual(await res.json(), { error: "email_not_verified" });
  });

  it("still reports bad credentials as a plain 401", async () => {
    globalThis.fetch = async () => new Response("{}", { status: 401 });
    const res = await POST(request());
    assert.equal(res.status, 401);
  });
});
