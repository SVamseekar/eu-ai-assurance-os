import type { Metadata } from "next";

import { CtaSection } from "@/components/landing/cta-section";
import { legalWebPageJsonLd } from "@/components/landing/legal-page-shell";
import {
  MarketingPageShell,
  PageIntro,
} from "@/components/landing/marketing-page-shell";
import { PersonasSection } from "@/components/landing/personas-section";
import { siteConfig } from "@/lib/site-config";

const title = "Who it's for";
const description =
  "Built for engineering, compliance, legal, data platform, product, and audit roles on the same release path.";

export const metadata: Metadata = {
  title,
  description,
  alternates: { canonical: "/who-its-for" },
  openGraph: {
    title: `${title} — ${siteConfig.name}`,
    description,
    url: `${siteConfig.url}/who-its-for`,
    type: "website",
  },
};

export default function WhoItsForPage() {
  return (
    <MarketingPageShell
      jsonLd={legalWebPageJsonLd({
        name: `${title} — ${siteConfig.name}`,
        description,
        path: "/who-its-for",
      })}
    >
      <PageIntro eyebrow="Roles" title={title} description={description} />
      <PersonasSection showHeading={false} />
      <CtaSection />
    </MarketingPageShell>
  );
}
