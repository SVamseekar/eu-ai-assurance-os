import { MarketingPageShell } from "@/components/landing/marketing-page-shell";
import { PricingTable } from "@/components/marketing/pricing-table";
import { Band, Container, PageHeader, SectionHeading } from "@/components/marketing/primitives";
import { CtaBand, TrustBadges } from "@/components/marketing/sections";
import { marketingMetadata, webPageJsonLd } from "@/lib/seo";
import { siteConfig } from "@/lib/site-config";

const title = "Pricing";
const description =
  "Start free with one gated AI system. Team and Business plans add systems, editors and signed evidence packs. Enterprise on request.";
const path = "/pricing";

export const metadata = marketingMetadata({
  title,
  description,
  path,
  keywords: ["AI governance pricing", "EU AI Act software pricing", "AI release gate pricing"],
});

const faqs = [
  { q: "Is this legal advice?", a: "No. Assurance OS provides evidence and readiness tooling. Your counsel decides how the law applies." },
  { q: "Do you certify compliance?", a: "No. We report evidence and readiness; notified bodies and your counsel make those determinations." },
  { q: "Who is the seller?", a: "Dodo Payments is the merchant of record and handles tax and invoices." },
  { q: "Can I cancel?", a: "Any time in the customer portal. Access continues to the end of the paid period." },
  { q: "What counts as a gated system?", a: "An AI system registered in Assurance OS that you gate in CI. Viewers are free." },
  { q: "What happens if I downgrade?", a: "Nothing is deleted. Systems beyond your plan become read-only." },
];

export default function PricingPage() {
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
        <Container className="pt-10 pb-20 sm:pt-12">
          <PricingTable
            header={
              <PageHeader
                tone="light"
                eyebrow="Pricing"
                title="Simple, transparent pricing."
                description="Start free, scale as you grow. No hidden fees."
              />
            }
          />
          <p className="mt-6 text-sm text-ink-muted">
            Prices in USD, excluding tax. Features listed as coming soon are not included until they ship.
          </p>
        </Container>
      </Band>

      <Band tone="light" muted className="border-y border-line">
        <Container className="py-12">
          <TrustBadges />
        </Container>
      </Band>

      <Band tone="light">
        <Container className="py-20 sm:py-24">
          <SectionHeading tone="light" eyebrow="FAQ" title="Questions about plans." />
          <dl className="mt-10 grid gap-x-10 gap-y-8 md:grid-cols-2">
            {faqs.map((f) => (
              <div key={f.q}>
                <dt className="font-semibold text-ink">{f.q}</dt>
                <dd className="mt-2 text-ink-muted">{f.a}</dd>
              </div>
            ))}
          </dl>
        </Container>
      </Band>

      <CtaBand />
    </MarketingPageShell>
  );
}
