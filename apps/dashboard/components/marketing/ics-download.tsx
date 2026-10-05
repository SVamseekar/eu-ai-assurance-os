"use client";

import { CalendarPlus } from "lucide-react";

import { mkButton } from "@/components/marketing/primitives";
import { DEADLINES } from "@/lib/deadlines";
import { deadlinesToIcs } from "@/lib/ics";

/** Builds the .ics file in the browser and downloads it. */
export function IcsDownload() {
  function download() {
    const blob = new Blob([deadlinesToIcs(DEADLINES)], { type: "text/calendar;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "ai-regulation-deadlines.ics";
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
  }
  return (
    <button type="button" onClick={download} className={mkButton.primary}>
      <CalendarPlus className="h-4 w-4" aria-hidden="true" />
      Add all dates to your calendar (.ics)
    </button>
  );
}
