import assert from "node:assert/strict";
import { afterEach, describe, it } from "node:test";
import { NextRequest } from "next/server";
import { POST } from "./route";

const realFetch = globalThis.fetch;
afterEach(() => {
  globalThis.fetch = realFetch;
});

const request = (body: unknown) =>
  new NextRequest("http://dashboard.test/api/auth/password/reset", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

describe("POST /api/auth/password/reset", () => {
  it("forwards token and new password and passes the status through", async () => {
    let sent = "";
    globalThis.fetch = async (_u, init) => {
      sent = String(init?.body);
      return new Response(null, { status: 204 });
    };
    const res = await POST(request({ token: "t", newPassword: "new-long-password-42", extra: 1 }));
    assert.equal(res.status, 204);
    assert.deepEqual(JSON.parse(sent), { token: "t", newPassword: "new-long-password-42" });
  });

  it("passes 410 and 400 through", async () => {
    globalThis.fetch = async () => new Response("{}", { status: 410 });
    assert.equal((await POST(request({ token: "t", newPassword: "new-long-password-42" }))).status, 410);
  });
});
