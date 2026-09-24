import { CapabilitiesSection } from "@/components/landing/capabilities-section";
import { CtaSection } from "@/components/landing/cta-section";
import {
  MarketingPageShell,
  PageIntro,
} from "@/components/landing/marketing-page-shell";
import { ProblemSection } from "@/components/landing/problem-section";
import { RelatedPages } from "@/components/landing/related-pages";
import { gateInputs, productNotThis } from "@/lib/landing-content";
import { marketingMetadata, webPageJsonLd } from "@/lib/seo";
import { siteConfig } from "@/lib/site-config";

const title = "EU AI Act control plane";
const description =
  "AI system registry, risk classification, cited evidence, eval gates, data-contract drift, approvals, and a sealed evidence pack in one release decision. Not a notified body.";
const path = "/product";
const crumbs = [
  { name: "Home", path: "/" },
  { name: "Product", path },
];

export const metadata = marketingMetadata({
  title,
  description,
  path,
  keywords: [
    "EU AI Act control plane",
    "AI system registry",
    "AI risk classification",
    "eval gates",
    "AI evidence pack",
  ],
});

export default function ProductPage() {
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
        eyebrow="Product"
        title="Everything a release decision needs, in one place"
        description={description}
        crumbs={[
          { href: "/", label: "Home" },
          { href: path, label: "Product" },
        ]}
      />

      <section className="mx-auto max-w-6xl px-4 py-16 sm:px-6">
        <h2 className="font-heading text-2xl font-semibold tracking-tight sm:text-3xl">
          What {siteConfig.shortName} is
        </h2>
        <div className="mt-4 max-w-3xl space-y-3 text-muted-foreground">
          <p>
            {siteConfig.name} is software for teams that ship AI into the EU
            market. It turns the system register, risk class, cited evidence,
            evals, data contracts, and approvals into one PASS, REVIEW, or
            BLOCKED decision — then seals the pack.
          </p>
          <p>
            Policy GRC tools inventory many frameworks. This product is a
            release gate. An in-force control uses INFORMATIONAL (the decision
            stays), WARNING (PASS becomes REVIEW), APPROVAL_REQUIRED (unsigned
            becomes REVIEW), or BLOCKING (the decision becomes BLOCKED). If
            cited evidence, the eval, or an open contract BREACH fails, the
            system does not pass.
          </p>
        </div>
        <ul className="mt-8 grid gap-4 sm:grid-cols-2">
          {gateInputs.map((item) => (
            <li
              key={item.title}
              className="rounded-xl border border-border bg-card p-5 shadow-sm"
            >
              <h3 className="font-heading text-base font-semibold">{item.title}</h3>
              <p className="mt-2 text-sm text-muted-foreground">{item.body}</p>
            </li>
          ))}
        </ul>
      </section>

      <ProblemSection />
      <CapabilitiesSection />

      <section className="border-y border-border bg-muted/40">
        <div className="mx-auto max-w-6xl px-4 py-16 sm:px-6">
          <h2 className="font-heading text-2xl font-semibold tracking-tight sm:text-3xl">
            What this is not
          </h2>
          <ul className="mt-8 grid gap-6 sm:grid-cols-3">
            {productNotThis.map((item) => (
              <li key={item.title}>
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
