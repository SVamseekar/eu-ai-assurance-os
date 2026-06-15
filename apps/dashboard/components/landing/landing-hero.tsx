import Link from "next/link";
import { CheckCircle2, AlertTriangle, FileCheck } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export function LandingHero() {
  return (
    <section aria-label="Introduction" className="mx-auto max-w-6xl px-4 pt-16 pb-20 sm:px-6 sm:pt-24 sm:pb-28">
      <div className="grid items-center gap-12 lg:grid-cols-2">
        <div>
          <h1 className="font-heading text-4xl font-semibold tracking-tight sm:text-5xl">
            Ship AI systems in the EU with evidence, not guesswork.
          </h1>
          <p className="mt-5 max-w-xl text-lg text-muted-foreground">
            EU AI Assurance OS turns risk classification, evidence, eval gates, and
            data-contract checks into a single release decision — PASS, REVIEW, or
            BLOCKED — backed by an audit-ready evidence pack.
          </p>
          <div className="mt-8 flex flex-wrap gap-3">
            <Button size="lg" render={<Link href="/command" />}>
              Open Dashboard
            </Button>
            <Button size="lg" variant="outline" render={<a href="#how-it-works" />}>
              See how it works
            </Button>
          </div>
        </div>
        <Card aria-label="Example release gate decision" className="ring-1 ring-foreground/10">
          <CardHeader className="flex-row items-center justify-between">
            <CardTitle>Claims Triage AI — Release Gate</CardTitle>
            <Badge variant="destructive">BLOCKED</Badge>
          </CardHeader>
          <CardContent className="space-y-3 font-mono text-sm">
            <div className="flex items-center gap-2">
              <CheckCircle2 className="h-4 w-4 text-emerald-600" aria-hidden="true" />
              Evidence coverage: 6 documents indexed
            </div>
            <div className="flex items-center gap-2">
              <CheckCircle2 className="h-4 w-4 text-emerald-600" aria-hidden="true" />
              Eval score: 0.93 (threshold 0.85)
            </div>
            <div className="flex items-center gap-2">
              <AlertTriangle className="h-4 w-4 text-destructive" aria-hidden="true" />
              Data contract: open BREACH on claims-intake-v2
            </div>
            <div className="flex items-center gap-2">
              <FileCheck className="h-4 w-4 text-muted-foreground" aria-hidden="true" />
              Human oversight SOP: on file
            </div>
          </CardContent>
        </Card>
      </div>
    </section>
  );
}
