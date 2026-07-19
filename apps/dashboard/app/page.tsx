import type { Metadata } from "next";

import { LandingHeader } from "@/components/landing/landing-header";
import { LandingHero } from "@/components/landing/landing-hero";
import { MetricsStrip } from "@/components/landing/metrics-strip";
import { ProblemSection } from "@/components/landing/problem-section";
import { CapabilitiesSection } from "@/components/landing/capabilities-section";
import { HowItWorksSection } from "@/components/landing/how-it-works-section";
import { PersonasSection } from "@/components/landing/personas-section";
import { TrustSection } from "@/components/landing/trust-section";
import { FaqSection } from "@/components/landing/faq-section";
import { CtaSection } from "@/components/landing/cta-section";
import { LandingFooter } from "@/components/landing/landing-footer";
import { faqItems } from "@/lib/landing-content";
import { siteConfig } from "@/lib/site-config";

export const metadata: Metadata = {
  title: "EU AI Assurance OS — EU AI Act Release Governance",
  description: siteConfig.description,
  alternates: { canonical: "/" },
  openGraph: {
    title: siteConfig.name,
    description: siteConfig.description,
    url: siteConfig.url,
    type: "website",
  },
};

function JsonLd() {
  const jsonLd = {
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
      },
      {
        "@type": "FAQPage",
        mainEntity: faqItems.map((item) => ({
          "@type": "Question",
          name: item.question,
          acceptedAnswer: {
            "@type": "Answer",
            text: item.answer,
          },
        })),
      },
    ],
  };

  return (
    <script
      type="application/ld+json"
      // eslint-disable-next-line react/no-danger
      dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
    />
  );
}

export default function LandingPage() {
  return (
    <>
      <JsonLd />
      <LandingHeader />
      <main>
        <LandingHero />
        <MetricsStrip />
        <ProblemSection />
        <CapabilitiesSection />
        <HowItWorksSection />
        <PersonasSection />
        <TrustSection />
        <FaqSection />
        <CtaSection />
      </main>
      <LandingFooter />
    </>
  );
}
