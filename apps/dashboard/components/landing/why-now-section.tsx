import { actSources, actTimeline } from "@/lib/landing-content";

export function WhyNowSection() {
  return (
    <section
      aria-labelledby="why-now-heading"
      className="border-b border-border"
    >
      <div className="mx-auto max-w-6xl px-4 py-16 sm:px-6">
        <h2
          id="why-now-heading"
          className="font-heading text-2xl font-semibold tracking-tight sm:text-3xl"
        >
          EU AI Act dates that matter now
        </h2>
        <p className="mt-3 max-w-2xl text-muted-foreground">
          Transparency duties are already live. High-risk Annex III work is the
          2027 clock — not a reason to wait on the register, evidence, and
          gates.
        </p>
        <ul className="mt-8 grid gap-4 md:grid-cols-3">
          {actTimeline.map((item) => (
            <li
              key={item.date}
              className="rounded-xl border border-border bg-card p-5 shadow-sm"
            >
              <p className="text-xs font-medium text-primary">{item.status}</p>
              <p className="mt-2 font-heading text-lg font-semibold tracking-tight">
                {item.date}
              </p>
              <h3 className="mt-1 font-heading text-base font-semibold">
                {item.title}
              </h3>
              <p className="mt-2 text-sm text-muted-foreground">{item.body}</p>
            </li>
          ))}
        </ul>
        <p className="mt-6 text-xs text-muted-foreground">
          Dates follow{" "}
          <a
            href={actSources.aiAct.href}
            target="_blank"
            rel="noopener noreferrer"
            className="underline-offset-2 hover:underline"
          >
            {actSources.aiAct.label}
          </a>{" "}
          as amended by{" "}
          <a
            href={actSources.omnibus.href}
            target="_blank"
            rel="noopener noreferrer"
            className="underline-offset-2 hover:underline"
          >
            {actSources.omnibus.label}
          </a>
          . Not legal advice.
        </p>
      </div>
    </section>
  );
}
