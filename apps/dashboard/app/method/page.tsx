import type { Metadata } from "next";
import Link from "next/link";

import { CtaSection } from "@/components/landing/cta-section";
import { legalWebPageJsonLd } from "@/components/landing/legal-page-shell";
import {
  MarketingPageShell,
  PageIntro,
} from "@/components/landing/marketing-page-shell";
import { publicClaimsMarketing } from "@/lib/landing-content";
import { siteConfig } from "@/lib/site-config";

const title = "Method";
const description =
  "What a fail-closed check sees from pages a company already published. Named organisations are examples, not customers, and not legal findings.";

export const metadata: Metadata = {
  title,
  description,
  alternates: { canonical: "/method" },
  openGraph: {
    title: `${title} — ${siteConfig.name}`,
    description,
    url: `${siteConfig.url}/method`,
    type: "website",
  },
};

export default function MethodPage() {
  return (
    <MarketingPageShell
      jsonLd={legalWebPageJsonLd({
        name: `${title} — ${siteConfig.name}`,
        description,
        path: "/method",
      })}
    >
      <PageIntro
        eyebrow="How we read public claims"
        title="What a fail-closed check sees from pages already published"
        description={description}
      />

      <section className="border-b border-border bg-amber-500/10">
        <div className="mx-auto max-w-6xl px-4 py-6 sm:px-6">
          <p className="text-sm text-foreground/90">
            Named organisations are <strong>not customers</strong>. The examples
            below are reconstructed from cited public pages only. This is not an
            accusation of non-compliance, not a notified-body assessment, and
            not a claim that they use this product. Annex III high-risk duties
            apply <strong>2 December 2027</strong>. Article 50 has applied since{" "}
            <strong>2 August 2026</strong>.
          </p>
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-4 py-16 sm:px-6">
        <h2 className="font-heading text-2xl font-semibold tracking-tight">
          How the check works
        </h2>
        <div className="mt-4 max-w-3xl space-y-3 text-sm text-muted-foreground">
          <p>
            Evgraph checks promotion files (model card, approval, deployment)
            and the dataset manifest. {siteConfig.name} stores those artifacts
            with the sealed evidence pack. If a public page does not publish an
            approval timestamp or a dataset license, the check does not pass. We
            do not invent missing fields.
          </p>
        </div>
      </section>

      <section className="border-y border-border bg-muted/30">
        <div className="mx-auto max-w-6xl px-4 py-16 sm:px-6">
          <h2 className="font-heading text-2xl font-semibold tracking-tight">
            Four public examples
          </h2>
          <p className="mt-3 max-w-3xl text-sm text-muted-foreground">
            Quotes and URLs come from pages those firms already published. A
            BLOCKED example is not a finding that the organisation is illegal.
          </p>
          <ul className="mt-8 grid gap-4 lg:grid-cols-2">
            {publicClaimsMarketing.map((item) => (
              <li
                key={item.slug}
                className="rounded-xl border border-border bg-card p-5 shadow-sm"
              >
                <p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                  {item.hq}
                </p>
                <h3 className="mt-1 font-heading text-base font-semibold">
                  {item.legalName}
                </h3>
                <p className="mt-2 text-sm text-muted-foreground">{item.hook}</p>
                <blockquote className="mt-4 border-l-2 border-primary/40 pl-3 text-sm italic text-foreground/90">
                  {item.quote}
                </blockquote>
                <p className="mt-3 text-sm text-muted-foreground">{item.evgraphNote}</p>
                <a
                  href={item.sourceUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="mt-4 inline-block text-sm font-medium text-primary underline-offset-2 hover:underline"
                >
                  Source: {item.sourceTitle}
                </a>
              </li>
            ))}
          </ul>
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-4 py-16 sm:px-6">
        <p className="max-w-3xl text-sm text-muted-foreground">
          Questions? See the{" "}
          <Link href="/faq" className="underline-offset-2 hover:underline">
            FAQ
          </Link>{" "}
          and{" "}
          <Link href="/disclaimer" className="underline-offset-2 hover:underline">
            Disclaimer
          </Link>
          .
        </p>
      </section>
      <CtaSection />
    </MarketingPageShell>
  );
}
