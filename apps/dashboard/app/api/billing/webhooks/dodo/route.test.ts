import assert from "node:assert/strict";
import { afterEach, describe, it } from "node:test";
import { NextRequest } from "next/server";
import { POST } from "./route";

const realFetch = globalThis.fetch;
afterEach(() => {
  globalThis.fetch = realFetch;
});

const body = '{"type":"subscription.active",  "data":{"a":1}}'; // odd spacing: must arrive byte for byte

const request = (headers: Record<string, string> = {}) =>
  new NextRequest("http://dashboard.test/api/billing/webhooks/dodo", { method: "POST", body, headers });

describe("POST /api/billing/webhooks/dodo", () => {
  it("forwards the exact body and the three signature headers", async () => {
    let sentBody = "";
    let sentHeaders: Record<string, string> = {};
    globalThis.fetch = async (_url, init) => {
      sentBody = new TextDecoder().decode(init?.body as ArrayBuffer);
      sentHeaders = init?.headers as Record<string, string>;
      return new Response(null, { status: 200 });
    };
    const res = await POST(
      request({ "webhook-id": "msg_1", "webhook-timestamp": "1700000000", "webhook-signature": "v1,abc", cookie: "x=y" }),
    );
    assert.equal(res.status, 200);
    assert.equal(sentBody, body);
    assert.equal(sentHeaders["webhook-id"], "msg_1");
    assert.equal(sentHeaders["webhook-timestamp"], "1700000000");
    assert.equal(sentHeaders["webhook-signature"], "v1,abc");
    assert.equal(sentHeaders["cookie"], undefined);
  });

  it("passes the API's refusal through so a bad signature is not acknowledged", async () => {
    globalThis.fetch = async () => new Response(null, { status: 401 });
    const res = await POST(request({ "webhook-id": "msg_1" }));
    assert.equal(res.status, 401);
  });

  it("answers 503 when the API is down so Dodo retries", async () => {
    globalThis.fetch = async () => {
      throw new Error("down");
    };
    const res = await POST(request());
    assert.equal(res.status, 503);
  });
});
