import type { Metadata } from "next";

import { CtaSection } from "@/components/landing/cta-section";
import { FaqSection } from "@/components/landing/faq-section";
import {
  MarketingPageShell,
  PageIntro,
} from "@/components/landing/marketing-page-shell";
import { faqItems } from "@/lib/landing-content";
import { siteConfig } from "@/lib/site-config";

const title = "FAQ";
const description =
  "What we sell, what Evgraph is, what is public in git, and what a release gate actually decides. Not legal advice.";

export const metadata: Metadata = {
  title,
  description,
  alternates: { canonical: "/faq" },
  openGraph: {
    title: `${title} — ${siteConfig.name}`,
    description,
    url: `${siteConfig.url}/faq`,
    type: "website",
  },
};

function jsonLd() {
  return {
    "@context": "https://schema.org",
    "@type": "FAQPage",
    name: `${title} — ${siteConfig.name}`,
    description,
    url: `${siteConfig.url}/faq`,
    mainEntity: faqItems.map((item) => ({
      "@type": "Question",
      name: item.question,
      acceptedAnswer: {
        "@type": "Answer",
        text: item.answer,
      },
    })),
  };
}

export default function FaqPage() {
  return (
    <MarketingPageShell jsonLd={jsonLd()}>
      <PageIntro eyebrow="FAQ" title="Frequently asked questions" description={description} />
      <FaqSection showHeading={false} />
      <CtaSection />
    </MarketingPageShell>
  );
}
