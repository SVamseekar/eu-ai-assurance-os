import assert from "node:assert/strict";
import { afterEach, beforeEach, describe, it } from "node:test";
import { NextRequest } from "next/server";
import { POST } from "./route";

const realFetch = globalThis.fetch;
let calls: { url: string; init?: RequestInit }[] = [];

function request(body: unknown, headers: Record<string, string> = {}) {
  return new NextRequest("http://dashboard.test/api/auth/signup", {
    method: "POST",
    headers: { "Content-Type": "application/json", ...headers },
    body: JSON.stringify(body),
  });
}

beforeEach(() => {
  calls = [];
  delete process.env.TURNSTILE_SECRET_KEY;
});
afterEach(() => {
  globalThis.fetch = realFetch;
  delete process.env.TURNSTILE_SECRET_KEY;
});

describe("POST /api/auth/signup", () => {
  it("forwards only email and organisation name, never a password", async () => {
    globalThis.fetch = async (url, init) => {
      calls.push({ url: String(url), init });
      return new Response(JSON.stringify({ status: "verification_sent" }), { status: 202 });
    };
    const res = await POST(
      request(
        { email: "a@b.example", organisationName: "Acme", password: "sneaky", turnstileToken: "t" },
        { "cf-connecting-ip": "9.9.9.9" },
      ),
    );
    assert.equal(res.status, 202);
    assert.deepEqual(await res.json(), { status: "verification_sent" });
    assert.equal(calls.length, 1);
    assert.deepEqual(JSON.parse(String(calls[0].init?.body)), { email: "a@b.example", organisationName: "Acme" });
    assert.equal((calls[0].init?.headers as Record<string, string>)["X-Client-IP"], "9.9.9.9");
  });

  it("refuses without calling the API when the challenge fails", async () => {
    process.env.TURNSTILE_SECRET_KEY = "secret";
    globalThis.fetch = async (url, init) => {
      calls.push({ url: String(url), init });
      return new Response(JSON.stringify({ success: false }));
    };
    const res = await POST(request({ email: "a@b.example", organisationName: "Acme" }));
    assert.equal(res.status, 400);
    assert.equal(calls.every((c) => c.url.includes("challenges.cloudflare.com")), true);
  });

  it("answers 503 when the API is unreachable", async () => {
    globalThis.fetch = async () => {
      throw new TypeError("fetch failed");
    };
    const res = await POST(request({ email: "a@b.example", organisationName: "Acme" }));
    assert.equal(res.status, 503);
  });
});
