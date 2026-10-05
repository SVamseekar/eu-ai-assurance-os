import { MarketingPageShell } from "@/components/landing/marketing-page-shell";
import { Band, Container, PageHeader } from "@/components/marketing/primitives";
import { CtaBand } from "@/components/marketing/sections";
import { UseCaseTabs } from "@/components/marketing/use-case-tabs";
import { marketingMetadata, webPageJsonLd } from "@/lib/seo";
import { siteConfig } from "@/lib/site-config";
import { useCases } from "@/lib/use-cases";

const title = "Use cases";
const description =
  "Built for the teams that ship and assure AI: engineering, compliance, legal, data, product and audit share one source of truth for every release.";
const path = "/who-its-for";

export const metadata = marketingMetadata({
  title,
  description,
  path,
  keywords: ["AI governance for engineering teams", "EU AI Act compliance team", "AI audit trail"],
});

export default function WhoItsForPage() {
  return (
    <MarketingPageShell
      jsonLd={webPageJsonLd({
        name: `${title} — ${siteConfig.shortName}`,
        description,
        path,
        crumbs: [
          { name: "Home", path: "/" },
          { name: title, path },
        ],
      })}
    >
      <Band tone="light">
        <Container className="pt-10 pb-20 sm:pt-12 sm:pb-24">
          <PageHeader
            tone="light"
            eyebrow="Use cases"
            title="Built for the teams that ship and assure AI."
            description="Different teams, one shared source of truth."
            className="max-w-5xl"
          />
          <div className="mt-10">
            <UseCaseTabs cases={useCases} />
          </div>
        </Container>
      </Band>
      <CtaBand />
    </MarketingPageShell>
  );
}
