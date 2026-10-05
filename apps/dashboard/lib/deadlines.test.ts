import assert from "node:assert/strict";
import { describe, it } from "node:test";

import { DEADLINES, deadlineStatus, nextDeadline } from "./deadlines";

describe("deadlines", () => {
  it("cites an https source for every date", () => {
    for (const d of DEADLINES) assert.match(d.source.url, /^https:\/\//, d.id);
  });

  it("marks Article 50 as applying now and Annex III as upcoming on launch day", () => {
    const launch = new Date("2026-11-10T00:00:00Z");
    const byId = Object.fromEntries(DEADLINES.map((d) => [d.id, d]));
    assert.equal(deadlineStatus(byId["eu-art50"], launch), "applies-now");
    assert.equal(deadlineStatus(byId["eu-annex-iii"], launch), "upcoming");
  });

  it("picks the soonest upcoming date for the deadline bar", () => {
    assert.equal(nextDeadline(new Date("2026-10-05T00:00:00Z"))?.id, "eu-art50-2");
    assert.equal(nextDeadline(new Date("2026-12-03T00:00:00Z"))?.id, "eu-annex-iii");
    assert.equal(nextDeadline(new Date("2030-01-01T00:00:00Z")), null);
  });
});
