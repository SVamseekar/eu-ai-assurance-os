import type { Metadata } from "next";

import { CtaSection } from "@/components/landing/cta-section";
import { HomeDestinations } from "@/components/landing/home-destinations";
import { LandingHero } from "@/components/landing/landing-hero";
import { MarketingPageShell } from "@/components/landing/marketing-page-shell";
import { MetricsStrip } from "@/components/landing/metrics-strip";
import { TrustSection } from "@/components/landing/trust-section";
import { siteConfig } from "@/lib/site-config";

export const metadata: Metadata = {
  title: "EU AI Assurance OS — fail-closed EU AI Act release gates",
  description: siteConfig.description,
  alternates: { canonical: "/" },
  openGraph: {
    title: siteConfig.name,
    description: siteConfig.description,
    url: siteConfig.url,
    type: "website",
  },
};

function jsonLd() {
  return {
    "@context": "https://schema.org",
    "@graph": [
      {
        "@type": "Organization",
        name: siteConfig.name,
        url: siteConfig.url,
        description: siteConfig.description,
        email: siteConfig.supportEmail,
      },
      {
        "@type": "WebSite",
        name: siteConfig.name,
        url: siteConfig.url,
        description: siteConfig.description,
        inLanguage: "en-GB",
        publisher: {
          "@type": "Organization",
          name: siteConfig.name,
          url: siteConfig.url,
        },
      },
      {
        "@type": "SoftwareApplication",
        name: siteConfig.name,
        applicationCategory: "BusinessApplication",
        operatingSystem: "Web",
        description: siteConfig.description,
        url: siteConfig.url,
        offers: {
          "@type": "Offer",
          name: "EU AI Act release readiness sprint",
          price: String(siteConfig.sprintPriceEur),
          priceCurrency: "EUR",
          url: `${siteConfig.url}/pricing`,
        },
      },
    ],
  };
}

export default function LandingPage() {
  return (
    <MarketingPageShell jsonLd={jsonLd()}>
      <LandingHero />
      <MetricsStrip />
      <HomeDestinations />
      <TrustSection />
      <CtaSection />
    </MarketingPageShell>
  );
}
