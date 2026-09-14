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
  "Public pages in, fail-closed checks out. Evgraph is a separate library. Named EU organisations are reconstructed teasers, not customers, and not legal findings.";

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
        eyebrow="Evgraph + public claims"
        title="What a fail-closed check sees from pages already published"
        description={description}
      />

      <section className="border-b border-border bg-amber-500/10">
        <div className="mx-auto max-w-6xl px-4 py-6 sm:px-6">
          <p className="text-sm text-foreground/90">
            Named organisations are <strong>not customers</strong>. Packs are
            reconstructed from cited public pages only. This is not an accusation
            of non-compliance, not a notified-body assessment, and not a claim
            that they use this product. Annex III high-risk duties apply{" "}
            <strong>2 December 2027</strong>. Article 50 has applied since{" "}
            <strong>2 August 2026</strong>.
          </p>
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-4 py-16 sm:px-6">
        <h2 className="font-heading text-2xl font-semibold tracking-tight">
          Evgraph is a library, not this repo
        </h2>
        <div className="mt-4 max-w-3xl space-y-3 text-sm text-muted-foreground">
          <p>
            Evgraph is a BSD-licensed Python package on PyPI. {siteConfig.name}{" "}
            does not vendor it. The API exports the JSON and CSV files the
            library already reads. The sprint runs:
          </p>
          <pre className="overflow-x-auto rounded-lg border border-border bg-muted/40 p-4 font-mono text-xs text-foreground">
            {`evgraph scan-promotion --model-card model_card.json \\
  --approval approval.json --deployment deployment.json \\
  --format markdown --gate --strict

evgraph scan-dataset-manifest dataset_manifest.csv \\
  --format markdown --gate`}
          </pre>
          <p>
            Public reconstructions <strong>must fail closed</strong>. We do not
            invent an <code>approved_at</code> timestamp to force a prettier
            result. Absence is the finding. Canonical packs live in{" "}
            <code>services/api/src/main/resources/public-claims/</code> in the
            public MIT repository.
          </p>
        </div>
      </section>

      <section className="border-y border-border bg-muted/30">
        <div className="mx-auto max-w-6xl px-4 py-16 sm:px-6">
          <h2 className="font-heading text-2xl font-semibold tracking-tight">
            Four sourced teasers
          </h2>
          <p className="mt-3 max-w-3xl text-sm text-muted-foreground">
            Quotes and URLs are taken from pages those firms already published.
            Sign in to a demo workspace to open the labelled registry rows. Do
            not treat BLOCKED as “illegal”.
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
        <h2 className="font-heading text-2xl font-semibold tracking-tight">
          What is public vs private
        </h2>
        <div className="mt-6 grid gap-6 sm:grid-cols-2">
          <div className="rounded-xl border border-border p-5">
            <h3 className="font-heading text-sm font-semibold">In the public git repo</h3>
            <ul className="mt-3 list-disc space-y-1 pl-5 text-sm text-muted-foreground">
              <li>Product source (dashboard, API, docs, infra skeletons)</li>
              <li>Sourced public-claims packs and this Method page</li>
              <li>Template DPA, MSA, and order form</li>
            </ul>
          </div>
          <div className="rounded-xl border border-border p-5">
            <h3 className="font-heading text-sm font-semibold">Not in git</h3>
            <ul className="mt-3 list-disc space-y-1 pl-5 text-sm text-muted-foreground">
              <li>Operator lab files (<code>.local/</code>), SSH, host IPs</li>
              <li>Secrets in <code>.env</code> / <code>.env.local</code></li>
              <li>Outreach queues, invoices, signed contracts, customer tenants</li>
              <li>
                Public-claims seeding on customer postgres (
                <code>ASSURANCE_PUBLIC_CLAIMS=false</code>)
              </li>
            </ul>
          </div>
        </div>
        <p className="mt-6 text-sm text-muted-foreground">
          See{" "}
          <Link href="/faq" className="underline-offset-2 hover:underline">
            FAQ
          </Link>
          , the{" "}
          <a
            href={siteConfig.githubUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="underline-offset-2 hover:underline"
          >
            public repository
          </a>
          , and{" "}
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
