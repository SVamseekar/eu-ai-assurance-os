import assert from "node:assert/strict";
import { afterEach, describe, it } from "node:test";
import { NextRequest } from "next/server";
import { POST } from "./route";

const realFetch = globalThis.fetch;
afterEach(() => {
  globalThis.fetch = realFetch;
  delete process.env.TURNSTILE_SECRET_KEY;
});

const request = (body: unknown) =>
  new NextRequest("http://dashboard.test/api/auth/password/forgot", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

describe("POST /api/auth/password/forgot", () => {
  it("answers 202 whatever the API says about the address", async () => {
    globalThis.fetch = async () => new Response("{}", { status: 404 });
    const res = await POST(request({ email: "ghost@nowhere.example" }));
    assert.equal(res.status, 202);
    assert.deepEqual(await res.json(), { status: "accepted" });
  });

  it("answers 503 when the API is down", async () => {
    globalThis.fetch = async () => {
      throw new TypeError("fetch failed");
    };
    assert.equal((await POST(request({ email: "a@b.example" }))).status, 503);
  });

  it("rejects a failed challenge before touching the API", async () => {
    process.env.TURNSTILE_SECRET_KEY = "secret";
    const urls: string[] = [];
    globalThis.fetch = async (u) => {
      urls.push(String(u));
      return new Response(JSON.stringify({ success: false }));
    };
    assert.equal((await POST(request({ email: "a@b.example" }))).status, 400);
    assert.equal(urls.some((u) => u.includes("/auth/password/forgot")), false);
  });
});
