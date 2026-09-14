import type { Metadata } from "next";

import { CapabilitiesSection } from "@/components/landing/capabilities-section";
import { CtaSection } from "@/components/landing/cta-section";
import {
  MarketingPageShell,
  PageIntro,
} from "@/components/landing/marketing-page-shell";
import { ProblemSection } from "@/components/landing/problem-section";
import { legalWebPageJsonLd } from "@/components/landing/legal-page-shell";
import { siteConfig } from "@/lib/site-config";

const title = "Product";
const description =
  "One control plane for AI system registry, guided risk class, cited evidence, eval gates, data-contract drift, approvals, and a sealed evidence pack. Evgraph is a separate library. Not a notified body.";

export const metadata: Metadata = {
  title,
  description,
  alternates: { canonical: "/product" },
  openGraph: {
    title: `${title} — ${siteConfig.name}`,
    description,
    url: `${siteConfig.url}/product`,
    type: "website",
  },
};

export default function ProductPage() {
  return (
    <MarketingPageShell
      jsonLd={legalWebPageJsonLd({
        name: `${title} — ${siteConfig.name}`,
        description,
        path: "/product",
      })}
    >
      <PageIntro
        eyebrow="Product"
        title="Everything a release decision needs, in one place"
        description={description}
      />
      <ProblemSection />
      <CapabilitiesSection showHeading={false} />
      <CtaSection />
    </MarketingPageShell>
  );
}
