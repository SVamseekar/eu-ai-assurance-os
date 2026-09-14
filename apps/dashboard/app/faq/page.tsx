import { CtaSection } from "@/components/landing/cta-section";
import { FaqSection } from "@/components/landing/faq-section";
import {
  MarketingPageShell,
  PageIntro,
} from "@/components/landing/marketing-page-shell";
import { RelatedPages } from "@/components/landing/related-pages";
import { faqItems } from "@/lib/landing-content";
import { marketingMetadata } from "@/lib/seo";
import { siteConfig } from "@/lib/site-config";

const title = "EU AI Act FAQ";
const description =
  "When Annex III high-risk duties apply, what Article 50 requires, how a release gate works, and how to start. Not legal advice.";
const path = "/faq";

export const metadata = marketingMetadata({
  title,
  description,
  path,
  keywords: [
    "EU AI Act FAQ",
    "Article 50",
    "Annex III high-risk",
    "notified body",
    "AI evidence pack",
  ],
});

function jsonLd() {
  return {
    "@context": "https://schema.org",
    "@graph": [
      {
        "@type": "FAQPage",
        name: `${title} — ${siteConfig.name}`,
        description,
        url: `${siteConfig.url}${path}`,
        inLanguage: "en-GB",
        mainEntity: faqItems.map((item) => ({
          "@type": "Question",
          name: item.question,
          acceptedAnswer: {
            "@type": "Answer",
            text: item.answer,
          },
        })),
      },
      {
        "@type": "BreadcrumbList",
        itemListElement: [
          {
            "@type": "ListItem",
            position: 1,
            name: "Home",
            item: siteConfig.url,
          },
          {
            "@type": "ListItem",
            position: 2,
            name: "FAQ",
            item: `${siteConfig.url}${path}`,
          },
        ],
      },
    ],
  };
}

export default function FaqPage() {
  return (
    <MarketingPageShell jsonLd={jsonLd()}>
      <PageIntro
        eyebrow="FAQ"
        title="Frequently asked questions"
        description={description}
        crumbs={[
          { href: "/", label: "Home" },
          { href: path, label: "FAQ" },
        ]}
      />
      <FaqSection showHeading={false} />
      <RelatedPages path={path} />
      <CtaSection />
    </MarketingPageShell>
  );
}
