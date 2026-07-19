import type { Metadata } from "next";
import Link from "next/link";
import { ShieldCheck } from "lucide-react";

import { DemoRequestForm } from "@/components/landing/demo-request-form";
import { LandingFooter } from "@/components/landing/landing-footer";
import { siteConfig } from "@/lib/site-config";

const title = "Request a demo";
const description =
  "Book a tailored walkthrough of EU AI Assurance OS — registry, evidence RAG, eval gates, contracts, and release decisions for EU AI Act governance.";

export const metadata: Metadata = {
  title,
  description,
  alternates: { canonical: "/request-demo" },
  openGraph: {
    title: `${title} — ${siteConfig.name}`,
    description,
    url: `${siteConfig.url}/request-demo`,
    type: "website",
  },
};

export default function RequestDemoPage() {
  const jsonLd = {
    "@context": "https://schema.org",
    "@type": "WebPage",
    name: `${title} — ${siteConfig.name}`,
    description,
    url: `${siteConfig.url}/request-demo`,
    isPartOf: {
      "@type": "WebSite",
      name: siteConfig.name,
      url: siteConfig.url,
    },
    inLanguage: "en-GB",
  };

  return (
    <>
      <script
        type="application/ld+json"
        // eslint-disable-next-line react/no-danger
        dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
      />
      <header className="sticky top-0 z-40 border-b border-border bg-background/80 backdrop-blur-md">
        <div className="mx-auto flex h-14 max-w-6xl items-center justify-between px-4 sm:px-6">
          <Link
            href="/"
            className="flex items-center gap-2 font-heading text-sm font-semibold"
          >
            <ShieldCheck className="h-4 w-4 text-primary" aria-hidden="true" />
            {siteConfig.name}
          </Link>
          <Link
            href="/login"
            className="text-sm text-muted-foreground hover:text-foreground"
          >
            Sign in
          </Link>
        </div>
      </header>
      <main className="mx-auto max-w-3xl px-4 py-12 sm:px-6 sm:py-16">
        <div className="mb-8">
          <h1 className="font-heading text-3xl font-semibold tracking-tight sm:text-4xl">
            Request a demo
          </h1>
          <p className="mt-3 max-w-2xl text-muted-foreground">
            See how {siteConfig.name} turns risk classification, evidence, eval
            gates, and data contracts into PASS / REVIEW / BLOCKED release
            decisions — without claiming legal certification.
          </p>
        </div>
        <DemoRequestForm />
      </main>
      <LandingFooter />
    </>
  );
}
