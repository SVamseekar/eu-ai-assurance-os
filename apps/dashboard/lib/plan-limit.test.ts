import assert from "node:assert/strict";
import { afterEach, describe, it } from "node:test";
import { PLAN_LIMIT_EVENT, reportPlanLimit } from "./plan-limit";

const g = globalThis as unknown as { window?: unknown; CustomEvent?: unknown };
const realWindow = g.window;
afterEach(() => {
  g.window = realWindow;
});

function fakeWindow() {
  const events: { type: string; detail: unknown }[] = [];
  g.window = {
    dispatchEvent: (e: { type: string; detail: unknown }) => {
      events.push({ type: e.type, detail: e.detail });
      return true;
    },
  };
  g.CustomEvent = class {
    constructor(public type: string, init: { detail: unknown }) {
      this.detail = init.detail;
    }
    detail: unknown;
  };
  return events;
}

describe("reportPlanLimit", () => {
  it("opens the prompt with the API's message on a 402", async () => {
    const events = fakeWindow();
    const res = new Response(JSON.stringify({ message: "Signed PDF needs Team." }), { status: 402 });
    assert.equal(await reportPlanLimit(res), "Signed PDF needs Team.");
    assert.deepEqual(events, [{ type: PLAN_LIMIT_EVENT, detail: "Signed PDF needs Team." }]);
  });
  it("falls back to a default message when the body is not JSON", async () => {
    const events = fakeWindow();
    assert.match((await reportPlanLimit(new Response("nope", { status: 402 })))!, /does not include/);
    assert.equal(events.length, 1);
  });
  it("ignores every other status", async () => {
    const events = fakeWindow();
    assert.equal(await reportPlanLimit(new Response("{}", { status: 500 })), null);
    assert.equal(events.length, 0);
  });
});
