import { MarketingPageShell } from "@/components/landing/marketing-page-shell";
import { AiActCheck } from "@/components/marketing/ai-act-check";
import { Band, Container, PageHeader } from "@/components/marketing/primitives";
import { marketingMetadata, webPageJsonLd } from "@/lib/seo";
import { siteConfig } from "@/lib/site-config";

const title = "Free EU AI Act applicability check";
const description =
  "Answer a few questions about your AI system and see its likely EU AI Act risk class, the obligations that may apply, and the dates that matter. No sign-up, nothing stored.";
const path = "/tools/ai-act-check";

export const metadata = marketingMetadata({
  title,
  description,
  path,
  keywords: ["EU AI Act checker", "EU AI Act risk classification", "is my AI high-risk", "Article 50 check", "Annex III check"],
});

export default function AiActCheckPage() {
  return (
    <MarketingPageShell
      jsonLd={[
        webPageJsonLd({
          name: `${title} — ${siteConfig.shortName}`,
          description,
          path,
          crumbs: [
            { name: "Home", path: "/" },
            { name: "AI Act check", path },
          ],
        }),
        {
          "@context": "https://schema.org",
          "@type": "WebApplication",
          name: title,
          url: `${siteConfig.url}${path}`,
          applicationCategory: "BusinessApplication",
          offers: { "@type": "Offer", price: 0, priceCurrency: "USD" },
        },
      ]}
    >
      <Band tone="light" muted>
        <Container className="pt-10 pb-20 sm:pt-12">
          <PageHeader
            tone="light"
            eyebrow="Free tool"
            title="Does the EU AI Act apply to your AI system?"
            description="Answer a short questionnaire. See the likely risk class, the obligations that may apply with their legal references, and the dates that matter. No sign-up, and your answers are not stored."
          />
          <div className="mt-10">
            <AiActCheck />
          </div>
        </Container>
      </Band>
    </MarketingPageShell>
  );
}
