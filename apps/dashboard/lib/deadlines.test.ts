import assert from "node:assert/strict";
import { describe, it } from "node:test";

import { DEADLINES, daysUntil, deadlineStatus, nextDeadline } from "./deadlines";

describe("deadlines", () => {
  it("lists exactly the dates from the launch spec", () => {
    assert.deepEqual(
      DEADLINES.map((d) => [d.id, d.date]),
      [
        ["eu-art50", "2026-08-02"],
        ["eu-art50-2", "2026-12-02"],
        ["au-adm", "2026-12-10"],
        ["us-co-admt", "2027-01-01"],
        ["eu-annex-iii", "2027-12-02"],
        ["eu-annex-i", "2028-08-02"],
      ],
    );
  });

  it("records when each source was checked", () => {
    for (const d of DEADLINES) assert.match(d.source.retrieved, /^\d{4}-\d{2}-\d{2}$/, d.id);
  });

  it("never shows a past date as upcoming", () => {
    const today = new Date();
    for (const d of DEADLINES) {
      if (daysUntil(d, today) <= 0) assert.equal(deadlineStatus(d, today), "applies-now", d.id);
    }
  });

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
    assert.equal(nextDeadline(new Date("2026-12-03T00:00:00Z"))?.id, "au-adm");
    assert.equal(nextDeadline(new Date("2027-01-02T00:00:00Z"))?.id, "eu-annex-iii");
    assert.equal(nextDeadline(new Date("2030-01-01T00:00:00Z")), null);
  });
});
