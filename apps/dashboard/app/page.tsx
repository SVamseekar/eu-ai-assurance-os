import { CtaSection } from "@/components/landing/cta-section";
import { HomeDestinations } from "@/components/landing/home-destinations";
import { LandingHero } from "@/components/landing/landing-hero";
import { MarketingPageShell } from "@/components/landing/marketing-page-shell";
import { MetricsStrip } from "@/components/landing/metrics-strip";
import { TrustSection } from "@/components/landing/trust-section";
import { WhyNowSection } from "@/components/landing/why-now-section";
import { marketingMetadata } from "@/lib/seo";
import { siteConfig } from "@/lib/site-config";

const title = "EU AI Act release gates";
const description = siteConfig.description;

export const metadata = marketingMetadata({
  title,
  description,
  path: "/",
  absoluteTitle: `${siteConfig.name} — fail-closed EU AI Act release gates`,
  keywords: [
    "EU AI Act compliance software",
    "AI release gate",
    "AI system registry",
    "Article 50",
    "Annex III high-risk",
  ],
});

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
        featureList: [
          "AI system registry",
          "EU AI Act risk classification",
          "Cited evidence",
          "Eval gates",
          "Data-contract drift",
          "Fail-closed promotion checks",
          "Sealed evidence pack",
        ],
      },
    ],
  };
}

export default function LandingPage() {
  return (
    <MarketingPageShell jsonLd={jsonLd()}>
      <LandingHero />
      <MetricsStrip />
      <WhyNowSection />
      <HomeDestinations />
      <TrustSection />
      <CtaSection />
    </MarketingPageShell>
  );
}
