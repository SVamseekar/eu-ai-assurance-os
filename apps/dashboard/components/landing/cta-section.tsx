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
          Request a tailored demo for your AI systems, or sign in if you already
          have a workspace.
        </p>
        <div className="mt-6 flex flex-wrap items-center justify-center gap-3">
          <Button size="lg" render={<Link href="/request-demo" />}>
            Request demo
          </Button>
          <Button size="lg" variant="outline" render={<Link href="/login" />}>
            Sign in
          </Button>
        </div>
      </div>
    </section>
  );
}
