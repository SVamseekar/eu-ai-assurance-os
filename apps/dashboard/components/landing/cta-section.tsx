import Link from "next/link";

import { Button } from "@/components/ui/button";

export function CtaSection() {
  return (
    <section aria-label="Get started" className="border-y border-border bg-muted/40">
      <div className="mx-auto max-w-6xl px-4 py-16 text-center sm:px-6">
        <h2 className="font-heading text-2xl font-semibold tracking-tight sm:text-3xl">
          Ready to see your release gate?
        </h2>
        <p className="mx-auto mt-3 max-w-xl text-muted-foreground">
          Open the dashboard to register a system, run an eval gate, and get your first
          PASS, REVIEW, or BLOCKED decision.
        </p>
        <Button size="lg" className="mt-6" render={<Link href="/command" />}>
          Open Dashboard
        </Button>
      </div>
    </section>
  );
}
