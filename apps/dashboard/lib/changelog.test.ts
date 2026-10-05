import assert from "node:assert/strict";
import { test } from "node:test";

import { loadChangelog, parseChangelog } from "./changelog";

test("parses entries newest first", () => {
  const entries = parseChangelog("# Changelog\n\n## 2026-09-30 — Old\n\n- a\n\n## 2026-10-04 — New\n\n- b\n- c\n");
  assert.deepEqual(
    entries.map((e) => [e.date, e.title, e.items.length]),
    [
      ["2026-10-04", "New", 2],
      ["2026-09-30", "Old", 1],
    ],
  );
});

test("the shipped changelog has dated entries with items", () => {
  const entries = loadChangelog();
  assert.ok(entries.length > 0);
  for (const e of entries) assert.ok(e.items.length > 0, e.title);
});
