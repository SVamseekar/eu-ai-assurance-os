import type { Metadata } from "next";

import { DemoRequestForm } from "@/components/landing/demo-request-form";
import { MarketingPageShell } from "@/components/landing/marketing-page-shell";
import { siteConfig } from "@/lib/site-config";

const title = "Request the sprint";
const description =
  "Request the €4,900 release-readiness sprint or a walkthrough of fail-closed gates, Evgraph artifacts, and sealed evidence packs. Not legal certification.";

export const metadata: Metadata = {
  title,
  description,
  alternates: { canonical: "/request-demo" },
  openGraph: {
    title: `${title} — ${siteConfig.name}`,
    description,
    url: `${siteConfig.url}/request-demo`,
    type: "website",
  },
};

export default function RequestDemoPage() {
  const jsonLd = {
    "@context": "https://schema.org",
    "@type": "WebPage",
    name: `${title} — ${siteConfig.name}`,
    description,
    url: `${siteConfig.url}/request-demo`,
    isPartOf: {
      "@type": "WebSite",
      name: siteConfig.name,
      url: siteConfig.url,
    },
    inLanguage: "en-GB",
  };

  return (
    <MarketingPageShell jsonLd={jsonLd}>
      <div className="mx-auto max-w-3xl px-4 py-12 sm:px-6 sm:py-16">
        <div className="mb-8">
          <h1 className="font-heading text-3xl font-semibold tracking-tight sm:text-4xl">
            Request the sprint
          </h1>
          <p className="mt-3 max-w-2xl text-muted-foreground">
            See how {siteConfig.name} turns risk classification, evidence, eval
            gates, Evgraph artifacts, and data contracts into PASS / REVIEW /
            BLOCKED — without claiming legal certification.
          </p>
        </div>
        <DemoRequestForm />
      </div>
    </MarketingPageShell>
  );
}
