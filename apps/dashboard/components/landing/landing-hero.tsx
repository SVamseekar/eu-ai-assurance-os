import Link from "next/link";
import { CheckCircle2, AlertTriangle, FileCheck } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { siteConfig } from "@/lib/site-config";

export function LandingHero() {
  return (
    <section
      aria-label="Introduction"
      className="relative overflow-hidden border-b border-border"
    >
      <div
        className="pointer-events-none absolute inset-0"
        aria-hidden="true"
        style={{
          background:
            "radial-gradient(ellipse 90% 70% at 10% -10%, color-mix(in oklch, var(--primary) 18%, transparent), transparent 55%), radial-gradient(ellipse 70% 50% at 90% 20%, color-mix(in oklch, var(--primary) 10%, transparent), transparent 50%), linear-gradient(180deg, color-mix(in oklch, var(--muted) 55%, transparent), transparent 40%)",
        }}
      />
      <div
        className="pointer-events-none absolute inset-0 opacity-[0.35]"
        aria-hidden="true"
        style={{
          backgroundImage:
            "linear-gradient(to right, color-mix(in oklch, var(--foreground) 6%, transparent) 1px, transparent 1px), linear-gradient(to bottom, color-mix(in oklch, var(--foreground) 6%, transparent) 1px, transparent 1px)",
          backgroundSize: "48px 48px",
          maskImage: "linear-gradient(180deg, black 30%, transparent 95%)",
        }}
      />

      <div className="relative mx-auto grid max-w-6xl items-center gap-12 px-4 pt-16 pb-20 sm:px-6 sm:pt-24 sm:pb-28 lg:grid-cols-2">
        <div>
          <p className="font-heading text-2xl font-semibold tracking-tight text-foreground sm:text-3xl">
            {siteConfig.name}
          </p>
          <h1 className="mt-4 max-w-xl font-heading text-3xl font-semibold tracking-tight sm:text-4xl">
            Ship AI systems in the EU with evidence, not guesswork.
          </h1>
          <p className="mt-5 max-w-xl text-lg text-muted-foreground">
            Risk classification, cited evidence, eval gates, and data-contract
            checks become one release decision — PASS, REVIEW, or BLOCKED —
            with an audit-ready evidence pack.
          </p>
          <div className="mt-8 flex flex-wrap gap-3">
            <Button size="lg" render={<Link href="/request-demo" />}>
              Request demo
            </Button>
            <Button size="lg" variant="outline" render={<Link href="/login" />}>
              Sign in
            </Button>
            <Button size="lg" variant="ghost" render={<a href="#how-it-works" />}>
              See how it works
            </Button>
          </div>
        </div>

        <div
          aria-label="Example release gate decision"
          className="border border-border/80 bg-background/80 p-5 font-mono text-sm backdrop-blur-sm"
        >
          <div className="mb-4 flex items-center justify-between gap-3 border-b border-border pb-3">
            <p className="font-heading text-sm font-semibold tracking-tight text-foreground">
              Claims Triage AI — Release Gate
            </p>
            <Badge variant="destructive">BLOCKED</Badge>
          </div>
          <ul className="space-y-3">
            <li className="flex items-center gap-2">
              <CheckCircle2 className="h-4 w-4 text-emerald-600" aria-hidden="true" />
              Evidence coverage: 6 documents indexed
            </li>
            <li className="flex items-center gap-2">
              <CheckCircle2 className="h-4 w-4 text-emerald-600" aria-hidden="true" />
              Eval score: 0.93 (threshold 0.85)
            </li>
            <li className="flex items-center gap-2">
              <AlertTriangle className="h-4 w-4 text-destructive" aria-hidden="true" />
              Data contract: open BREACH on claims-intake-v2
            </li>
            <li className="flex items-center gap-2">
              <FileCheck className="h-4 w-4 text-muted-foreground" aria-hidden="true" />
              Human oversight SOP: on file
            </li>
          </ul>
        </div>
      </div>
    </section>
  );
}
