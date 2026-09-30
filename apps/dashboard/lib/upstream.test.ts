import assert from "node:assert/strict";
import { afterEach, describe, it } from "node:test";
import { fetchUpstream } from "./upstream";

const realFetch = globalThis.fetch;
afterEach(() => {
  globalThis.fetch = realFetch;
});

describe("fetchUpstream", () => {
  it("returns the response when the API answers", async () => {
    globalThis.fetch = async () => new Response("{}", { status: 200 });
    const res = await fetchUpstream("http://api.test/x");
    assert.equal(res?.status, 200);
  });

  it("returns null when the API is unreachable", async () => {
    globalThis.fetch = async () => {
      throw new TypeError("fetch failed");
    };
    assert.equal(await fetchUpstream("http://api.test/x"), null);
  });

  it("returns null when the API does not answer within the timeout", async () => {
    globalThis.fetch = (_url, init) =>
      new Promise((_resolve, reject) => {
        (init?.signal as AbortSignal).addEventListener("abort", () => reject(new Error("aborted")));
      });
    assert.equal(await fetchUpstream("http://api.test/x", undefined, 50), null);
  });
});
