const problems = [
  {
    title: "Evidence chasing before every audit",
    description:
      "DPIAs, model cards, and vendor docs live in shared drives and inboxes, scattered across teams — until someone has to find them all at once.",
  },
  {
    title: "Unclear obligations per risk tier",
    description:
      "Each risk classification implies a different set of controls, but tracking which apply to which system is manual and easy to get wrong.",
  },
  {
    title: "No single release decision",
    description:
      "Eval scores, contract drift, and approvals live in different tools, so “is this safe to ship?” doesn't have one clear answer.",
  },
];

export function ProblemSection() {
  return (
    <section aria-label="The problem" className="border-y border-border bg-muted/40">
      <div className="mx-auto max-w-6xl px-4 py-16 sm:px-6">
        <h2 className="font-heading text-2xl font-semibold tracking-tight sm:text-3xl">
          Releasing AI in the EU shouldn't mean a fire drill
        </h2>
        <div className="mt-8 grid gap-8 sm:grid-cols-3">
          {problems.map((problem) => (
            <div key={problem.title}>
              <h3 className="font-heading text-base font-semibold">{problem.title}</h3>
              <p className="mt-2 text-sm text-muted-foreground">{problem.description}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
