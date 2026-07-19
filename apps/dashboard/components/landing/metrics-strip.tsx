import { landingMetricChips, measuredScaleLine } from "@/lib/metrics-canon";

export function MetricsStrip() {
  return (
    <section
      aria-label="Product facts"
      className="border-y border-border bg-muted/30"
    >
      <div className="mx-auto max-w-6xl px-4 py-10 sm:px-6">
        <p className="text-center text-xs font-semibold uppercase tracking-wider text-muted-foreground">
          Built for EU AI Act release governance
        </p>
        <ul className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {landingMetricChips.map((chip) => (
            <li
              key={chip.label}
              className="rounded-xl border border-border bg-card px-4 py-4 shadow-sm"
              title={chip.detail}
            >
              <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                {chip.label}
              </p>
              <p className="mt-1.5 font-heading text-sm font-semibold leading-snug sm:text-[15px]">
                {chip.value}
              </p>
            </li>
          ))}
        </ul>
        <p className="mt-4 text-center text-[11px] text-muted-foreground">
          Product facts from code freeze — {measuredScaleLine}. Not legal
          certification or notified-body status.
        </p>
      </div>
    </section>
  );
}
