import { CtaSection } from "@/components/landing/cta-section";
import { HowItWorksSection } from "@/components/landing/how-it-works-section";
import {
  MarketingPageShell,
  PageIntro,
} from "@/components/landing/marketing-page-shell";
import { RelatedPages } from "@/components/landing/related-pages";
import { packContents, releaseDecisionMeanings } from "@/lib/landing-content";
import { marketingMetadata, webPageJsonLd } from "@/lib/seo";
import { siteConfig } from "@/lib/site-config";

const title = "How an EU AI Act release gate works";
const description =
  "Register the system, classify risk against a pinned corpus, queue mapping proposals until a person accepts them, then take PASS, REVIEW, or BLOCKED. The pack and evgraph-cli 0.1.2 share the current gap.";
const path = "/how-it-works";
const crumbs = [
  { name: "Home", path: "/" },
  { name: "How it works", path },
];

export const metadata = marketingMetadata({
  title,
  description,
  path,
  keywords: [
    "EU AI Act release gate",
    "PASS REVIEW BLOCKED",
    "AI evidence pack",
    "how EU AI Act compliance works",
  ],
});

export default function HowItWorksPage() {
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
        eyebrow="How it works"
        title={title}
        description={description}
        crumbs={[
          { href: "/", label: "Home" },
          { href: path, label: "How it works" },
        ]}
      />
      <HowItWorksSection />

      <section className="mx-auto max-w-6xl px-4 py-16 sm:px-6">
        <h2 className="font-heading text-2xl font-semibold tracking-tight sm:text-3xl">
          What PASS, REVIEW, and BLOCKED mean
        </h2>
        <p className="mt-3 max-w-2xl text-muted-foreground">
          One decision per system. An in-force control applies INFORMATIONAL,
          WARNING, APPROVAL_REQUIRED, or BLOCKING before that decision is sealed.
        </p>
        <ul className="mt-8 grid gap-4 md:grid-cols-3">
          {releaseDecisionMeanings.map((item) => (
            <li
              key={item.decision}
              className="rounded-xl border border-border bg-card p-5 shadow-sm"
            >
              <h3 className="font-heading text-base font-semibold">{item.decision}</h3>
              <p className="mt-2 text-sm text-muted-foreground">{item.meaning}</p>
            </li>
          ))}
        </ul>
      </section>

      <section className="border-y border-border bg-muted/40">
        <div className="mx-auto max-w-6xl px-4 py-16 sm:px-6">
          <h2 className="font-heading text-2xl font-semibold tracking-tight sm:text-3xl">
            What the sealed pack includes
          </h2>
          <p className="mt-3 max-w-2xl text-muted-foreground">
            JSON plus a hashed PDF you can hand to an auditor. It is a work
            product, not a legal Annex IV filing.
          </p>
          <ul className="mt-8 grid gap-3 sm:grid-cols-2">
            {packContents.map((item) => (
              <li
                key={item}
                className="rounded-lg border border-border bg-card px-4 py-3 text-sm"
              >
                {item}
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
