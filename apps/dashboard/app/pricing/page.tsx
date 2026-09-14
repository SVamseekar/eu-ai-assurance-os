import type { Metadata } from "next";
import Link from "next/link";

import { CtaSection } from "@/components/landing/cta-section";
import {
  MarketingPageShell,
  PageIntro,
} from "@/components/landing/marketing-page-shell";
import { Button } from "@/components/ui/button";
import { siteConfig } from "@/lib/site-config";

const title = "Pricing";
const description = `EU AI Act release-readiness sprint: €${siteConfig.sprintPriceEur.toLocaleString("en-GB")} net for one named AI system, ${siteConfig.sprintDays} working days. Not a certificate. Not Stripe self-serve.`;

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
    "@graph": [
      {
        "@type": "WebPage",
        name: `${title} — ${siteConfig.name}`,
        description,
        url: `${siteConfig.url}/pricing`,
        inLanguage: "en-GB",
      },
      {
        "@type": "Offer",
        name: "EU AI Act release readiness sprint",
        description,
        price: String(siteConfig.sprintPriceEur),
        priceCurrency: "EUR",
        url: `${siteConfig.url}/pricing`,
        seller: {
          "@type": "Organization",
          name: siteConfig.name,
          url: siteConfig.url,
        },
      },
    ],
  };
}

export default function PricingPage() {
  const price = `€${siteConfig.sprintPriceEur.toLocaleString("en-GB")}`;
  const extra = `€${siteConfig.sprintExtraSystemEur.toLocaleString("en-GB")}`;

  return (
    <MarketingPageShell jsonLd={jsonLd()}>
      <PageIntro eyebrow="Commercial offer" title="Readiness sprint, not a subscription" description={description} />
      <section className="mx-auto max-w-6xl px-4 py-16 sm:px-6">
        <div className="grid gap-8 lg:grid-cols-[2fr_1fr]">
          <div className="rounded-xl border border-border bg-card p-6 shadow-sm sm:p-8">
            <p className="text-xs font-semibold uppercase tracking-wider text-primary">
              SKU
            </p>
            <h2 className="mt-2 font-heading text-2xl font-semibold tracking-tight">
              Release readiness sprint
            </h2>
            <p className="mt-2 text-3xl font-semibold tracking-tight">{price} net</p>
            <p className="mt-1 text-sm text-muted-foreground">
              One named AI system · {siteConfig.sprintDays} working days · 50% to
              start, 50% on delivery
            </p>
            <ul className="mt-6 space-y-3 text-sm text-foreground/90">
              <li>Evgraph library scan (approval-before-deploy, dataset license) — you keep the CLI</li>
              <li>
                System registered in {siteConfig.name}: risk class, assisted
                obligation map, PASS / REVIEW / BLOCKED
              </li>
              <li>Sealed evidence pack (JSON + PDF hash) including Annex IV-shaped checklist</li>
              <li>Optional CI snippets for both gates</li>
              <li>60-minute readout</li>
            </ul>
            <p className="mt-6 text-sm text-muted-foreground">
              Extra system in the same sprint: {extra}.
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Button render={<Link href="/request-demo" />}>Request the sprint</Button>
              <Button variant="outline" render={<Link href="/order-form" />}>
                Order form
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
                <li>Stripe self-serve subscription</li>
              </ul>
            </div>
            <div className="rounded-xl border border-border p-6">
              <h2 className="font-heading text-base font-semibold">How to order</h2>
              <p className="mt-3 text-sm text-muted-foreground">
                Email{" "}
                <a
                  href={`mailto:${siteConfig.supportEmail}`}
                  className="text-foreground underline-offset-2 hover:underline"
                >
                  {siteConfig.supportEmail}
                </a>{" "}
                with the system name and billing entity. We send an invoice and a
                workspace invite. Execute the{" "}
                <Link href="/msa" className="underline-offset-2 hover:underline">
                  MSA
                </Link>{" "}
                and{" "}
                <Link href="/dpa" className="underline-offset-2 hover:underline">
                  DPA
                </Link>{" "}
                templates after counsel review.
              </p>
            </div>
          </aside>
        </div>
      </section>
      <CtaSection />
    </MarketingPageShell>
  );
}
