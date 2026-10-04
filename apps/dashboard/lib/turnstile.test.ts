import assert from "node:assert/strict";
import { afterEach, describe, it } from "node:test";
import { verifyTurnstile } from "./turnstile";

const realFetch = globalThis.fetch;
afterEach(() => {
  globalThis.fetch = realFetch;
});

describe("verifyTurnstile", () => {
  it("accepts when Cloudflare says success", async () => {
    globalThis.fetch = async () => new Response(JSON.stringify({ success: true }));
    assert.equal(await verifyTurnstile("tok", "1.2.3.4", "secret"), true);
  });
  it("rejects when Cloudflare says failure", async () => {
    globalThis.fetch = async () => new Response(JSON.stringify({ success: false }));
    assert.equal(await verifyTurnstile("tok", null, "secret"), false);
  });
  it("sends the secret, token and client IP to Cloudflare", async () => {
    let sent: URLSearchParams | null = null;
    globalThis.fetch = async (_url, init) => {
      sent = init?.body as URLSearchParams;
      return new Response(JSON.stringify({ success: true }));
    };
    await verifyTurnstile("tok", "1.2.3.4", "secret");
    assert.equal(sent!.get("secret"), "secret");
    assert.equal(sent!.get("response"), "tok");
    assert.equal(sent!.get("remoteip"), "1.2.3.4");
  });
  it("rejects a missing token without calling Cloudflare", async () => {
    let called = false;
    globalThis.fetch = async () => {
      called = true;
      return new Response("{}");
    };
    assert.equal(await verifyTurnstile(null, null, "secret"), false);
    assert.equal(called, false);
  });
  it("rejects when Cloudflare is unreachable", async () => {
    globalThis.fetch = async () => {
      throw new Error("down");
    };
    assert.equal(await verifyTurnstile("tok", null, "secret"), false);
  });
  it("is disabled when no secret is configured outside production", async () => {
    assert.equal(await verifyTurnstile(null, null, ""), true);
  });
  it("fails closed when no secret is configured in production", async () => {
    const env = process.env as Record<string, string | undefined>;
    const before = env.NODE_ENV;
    env.NODE_ENV = "production";
    try {
      assert.equal(await verifyTurnstile("tok", null, ""), false);
    } finally {
      env.NODE_ENV = before;
    }
  });
});
