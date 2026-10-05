import assert from "node:assert/strict";
import { describe, it } from "node:test";

import { DEADLINES } from "./deadlines";
import { deadlinesToIcs } from "./ics";

describe("deadlinesToIcs", () => {
  const ics = deadlinesToIcs(DEADLINES, new Date("2026-10-05T12:00:00Z"));

  it("has one all-day event per deadline", () => {
    assert.equal(ics.match(/BEGIN:VEVENT/g)?.length, DEADLINES.length);
    assert.match(ics, /DTSTART;VALUE=DATE:20261202\r\nDTEND;VALUE=DATE:20261203/);
  });

  it("uses CRLF line endings and escapes commas", () => {
    assert.ok(ics.startsWith("BEGIN:VCALENDAR\r\n"));
    assert.ok(!/[^\r]\n/.test(ics));
    assert.match(ics, /SUMMARY:Colorado SB 26-189: Automated decision-making technology disclosures/);
  });
});
