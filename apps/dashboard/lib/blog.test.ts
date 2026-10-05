import assert from "node:assert/strict";
import { describe, it } from "node:test";

import { POSTS, sortedPosts } from "./blog";

const BANNED = /\b(compliant|certified|guarantees?)\b/i;

describe("blog posts", () => {
  it("have unique slugs and ISO dates", () => {
    const slugs = new Set(POSTS.map((p) => p.slug));
    assert.equal(slugs.size, POSTS.length);
    for (const p of POSTS) assert.match(p.published, /^\d{4}-\d{2}-\d{2}$/, p.slug);
  });

  it("follow the copy rules", () => {
    for (const p of POSTS) {
      const text = [p.title, p.excerpt, ...p.body.map((b) => ("text" in b ? b.text : b.items.join(" ")))].join(" ");
      assert.doesNotMatch(text, BANNED, p.slug);
    }
  });

  it("cite https sources", () => {
    for (const p of POSTS) for (const s of p.sources) assert.match(s.url, /^https:\/\//, p.slug);
  });

  it("list newest first", () => {
    const dates = sortedPosts().map((p) => p.published);
    assert.deepEqual(dates, [...dates].sort().reverse());
  });
});
