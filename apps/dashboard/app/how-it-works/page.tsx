import type { Metadata } from "next";

import { CtaSection } from "@/components/landing/cta-section";
import { HowItWorksSection } from "@/components/landing/how-it-works-section";
import { legalWebPageJsonLd } from "@/components/landing/legal-page-shell";
import {
  MarketingPageShell,
  PageIntro,
} from "@/components/landing/marketing-page-shell";
import { siteConfig } from "@/lib/site-config";

const title = "How it works";
const description =
  "Register the AI system, classify risk, run Evgraph plus eval and contract gates, then take a PASS / REVIEW / BLOCKED decision with a sealed pack.";

export const metadata: Metadata = {
  title,
  description,
  alternates: { canonical: "/how-it-works" },
  openGraph: {
    title: `${title} — ${siteConfig.name}`,
    description,
    url: `${siteConfig.url}/how-it-works`,
    type: "website",
  },
};

export default function HowItWorksPage() {
  return (
    <MarketingPageShell
      jsonLd={legalWebPageJsonLd({
        name: `${title} — ${siteConfig.name}`,
        description,
        path: "/how-it-works",
      })}
    >
      <PageIntro eyebrow="Method of work" title={title} description={description} />
      <HowItWorksSection showHeading={false} />
      <CtaSection />
    </MarketingPageShell>
  );
}
