import type { Metadata } from "next";
import Link from "next/link";

import { CtaSection } from "@/components/landing/cta-section";
import {
  MarketingPageShell,
  PageIntro,
} from "@/components/landing/marketing-page-shell";
import { Button } from "@/components/ui/button";
import { siteConfig } from "@/lib/site-config";

const title = "How we work";
const description =
  "Start with a demo. We scope one named AI system and send a written quote. Not a self-serve subscription and not a certificate.";

export const metadata: Metadata = {
  title,
  description,
  alternates: { canonical: "/pricing" },
  openGraph: {
    title: `${title} — ${siteConfig.name}`,
    description,
    url: `${siteConfig.url}/pricing`,
    type: "website",
  },
};

function jsonLd() {
  return {
    "@context": "https://schema.org",
    "@type": "WebPage",
    name: `${title} — ${siteConfig.name}`,
    description,
    url: `${siteConfig.url}/pricing`,
    inLanguage: "en-GB",
  };
}

export default function PricingPage() {
  return (
    <MarketingPageShell jsonLd={jsonLd()}>
      <PageIntro
        eyebrow="Commercial"
        title="Start with a demo"
        description={description}
      />
      <section className="mx-auto max-w-6xl px-4 py-16 sm:px-6">
        <div className="grid gap-8 lg:grid-cols-[2fr_1fr]">
          <div className="rounded-xl border border-border bg-card p-6 shadow-sm sm:p-8">
            <p className="text-xs font-semibold uppercase tracking-wider text-primary">
              Typical first engagement
            </p>
            <h2 className="mt-2 font-heading text-2xl font-semibold tracking-tight">
              Release readiness sprint
            </h2>
            <p className="mt-3 text-sm text-muted-foreground">
              One named AI system. After a short call we send a written quote
              with fees and term.
            </p>
            <ul className="mt-6 space-y-3 text-sm text-foreground/90">
              <li>Evgraph library scan (approval-before-deploy, dataset license) — you keep the CLI</li>
              <li>
                System registered in {siteConfig.name}: risk class, assisted
                obligation map, PASS / REVIEW / BLOCKED
              </li>
              <li>Sealed evidence pack (JSON + PDF hash) including Annex IV-shaped checklist</li>
              <li>Optional CI snippets for both gates</li>
              <li>Readout with the people who own the release</li>
            </ul>
            <div className="mt-8 flex flex-wrap gap-3">
              <Button render={<Link href="/request-demo" />}>Request a demo</Button>
              <Button variant="outline" render={<Link href="/order-form" />}>
                Order form template
              </Button>
            </div>
          </div>
          <aside className="space-y-6">
            <div className="rounded-xl border border-border bg-muted/40 p-6">
              <h2 className="font-heading text-base font-semibold">Not included</h2>
              <ul className="mt-3 list-disc space-y-1 pl-5 text-sm text-muted-foreground">
                <li>Legal certification or notified-body assessment</li>
                <li>Annex IV as a legal instrument</li>
                <li>FRIA as legal advice</li>
                <li>EU database registration</li>
                <li>Self-serve subscription checkout</li>
              </ul>
            </div>
            <div className="rounded-xl border border-border p-6">
              <h2 className="font-heading text-base font-semibold">How to start</h2>
              <p className="mt-3 text-sm text-muted-foreground">
                Request a demo, or email{" "}
                <a
                  href={`mailto:${siteConfig.supportEmail}`}
                  className="text-foreground underline-offset-2 hover:underline"
                >
                  {siteConfig.supportEmail}
                </a>{" "}
                with the system name. Counsel reviews the{" "}
                <Link href="/msa" className="underline-offset-2 hover:underline">
                  MSA
                </Link>{" "}
                and{" "}
                <Link href="/dpa" className="underline-offset-2 hover:underline">
                  DPA
                </Link>{" "}
                templates before signature. Fees are only those in the quote.
              </p>
            </div>
          </aside>
        </div>
      </section>
      <CtaSection />
    </MarketingPageShell>
  );
}
