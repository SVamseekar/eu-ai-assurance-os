import Link from "next/link";

import { DemoRequestForm } from "@/components/landing/demo-request-form";
import { MarketingPageShell } from "@/components/landing/marketing-page-shell";
import { RelatedPages } from "@/components/landing/related-pages";
import { marketingMetadata, webPageJsonLd } from "@/lib/seo";
import { siteConfig } from "@/lib/site-config";

const title = "Request a demo";
const description =
  "Walk through a release gate on one named system: a pinned corpus, a proposal queue, and a sealed pack whose gap matches evgraph-cli 0.1.2. Work is scoped after the call. Not legal certification.";
const path = "/request-demo";
const crumbs = [
  { name: "Home", path: "/" },
  { name: "Request a demo", path },
];

export const metadata = marketingMetadata({
  title,
  description,
  path,
  keywords: [
    "EU AI Act demo",
    "AI governance demo",
    "request demo release gate",
  ],
});

export default function RequestDemoPage() {
  return (
    <MarketingPageShell
      jsonLd={webPageJsonLd({
        name: `${title} — ${siteConfig.name}`,
        description,
        path,
        crumbs,
      })}
    >
      <div className="mx-auto max-w-3xl px-4 py-12 sm:px-6 sm:py-16">
        <nav aria-label="Breadcrumb" className="mb-6 text-sm text-muted-foreground">
          <ol className="flex flex-wrap items-center gap-1.5">
            <li>
              <Link href="/" className="hover:text-foreground">
                Home
              </Link>
            </li>
            <li className="flex items-center gap-1.5">
              <span aria-hidden>/</span>
              <span className="text-foreground">Request a demo</span>
            </li>
          </ol>
        </nav>
        <h1 className="font-heading text-3xl font-semibold tracking-tight sm:text-4xl">
          Request a demo
        </h1>
        <p className="mt-3 max-w-2xl text-muted-foreground">{description}</p>
        <ul className="mt-6 space-y-2 text-sm text-foreground/90">
          <li>One named system — owner, purpose, data, where it runs.</li>
          <li>A PASS / REVIEW / BLOCKED walkthrough, including what fails closed.</li>
          <li>Written quote after the call. No public price list.</li>
        </ul>
        <div className="mt-8">
          <DemoRequestForm />
        </div>
      </div>
      <RelatedPages path={path} />
    </MarketingPageShell>
  );
}
