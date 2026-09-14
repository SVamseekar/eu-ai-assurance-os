import { CtaSection } from "@/components/landing/cta-section";
import {
  MarketingPageShell,
  PageIntro,
} from "@/components/landing/marketing-page-shell";
import { PersonasSection } from "@/components/landing/personas-section";
import { RelatedPages } from "@/components/landing/related-pages";
import { typicalSectors } from "@/lib/landing-content";
import { marketingMetadata, webPageJsonLd } from "@/lib/seo";
import { siteConfig } from "@/lib/site-config";

const title = "Who EU AI Act release gates are for";
const description =
  "Engineering, compliance, legal, data, product, and audit on the same release path. Typical first systems: claims, credit, recruiting, and internal assistants.";
const path = "/who-its-for";
const crumbs = [
  { name: "Home", path: "/" },
  { name: "Who it's for", path },
];

export const metadata = marketingMetadata({
  title,
  description,
  path,
  keywords: [
    "EU AI Act compliance team",
    "AI governance roles",
    "high-risk AI employment",
    "AI in insurance credit recruitment",
  ],
});

export default function WhoItsForPage() {
  return (
    <MarketingPageShell
      jsonLd={webPageJsonLd({
        name: `${title} — ${siteConfig.name}`,
        description,
        path,
        crumbs,
      })}
    >
      <PageIntro
        eyebrow="Roles"
        title={title}
        description={description}
        crumbs={[
          { href: "/", label: "Home" },
          { href: path, label: "Who it's for" },
        ]}
      />
      <PersonasSection />

      <section className="border-y border-border bg-muted/40">
        <div className="mx-auto max-w-6xl px-4 py-16 sm:px-6">
          <h2 className="font-heading text-2xl font-semibold tracking-tight sm:text-3xl">
            Where teams usually start
          </h2>
          <p className="mt-3 max-w-2xl text-muted-foreground">
            One named system, not the whole estate. These are common first
            systems in the EU market — not a list of customers.
          </p>
          <ul className="mt-8 grid gap-4 sm:grid-cols-2">
            {typicalSectors.map((item) => (
              <li
                key={item.title}
                className="rounded-xl border border-border bg-card p-5 shadow-sm"
              >
                <h3 className="font-heading text-base font-semibold">{item.title}</h3>
                <p className="mt-2 text-sm text-muted-foreground">{item.body}</p>
              </li>
            ))}
          </ul>
        </div>
      </section>

      <RelatedPages path={path} />
      <CtaSection />
    </MarketingPageShell>
  );
}
