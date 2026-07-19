import Link from "next/link";
import type { ReactNode } from "react";

import { siteConfig } from "@/lib/site-config";

type LegalPageShellProps = {
  title: string;
  description: string;
  children: ReactNode;
};

export function LegalPageShell({ title, description, children }: LegalPageShellProps) {
  return (
    <div className="min-h-full bg-background">
      <header className="border-b border-border">
        <div className="mx-auto flex h-14 max-w-3xl items-center px-4 sm:px-6">
          <nav aria-label="Site">
            <Link
              href="/"
              className="text-sm font-medium text-muted-foreground hover:text-foreground"
            >
              ← {siteConfig.name} home
            </Link>
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-3xl px-4 py-12 sm:px-6 sm:py-16">
        <article className="prose-legal">
          <h1 className="font-heading text-3xl font-semibold tracking-tight">{title}</h1>
          <p className="mt-2 text-sm text-muted-foreground">
            Last updated: {siteConfig.legalLastUpdated}
          </p>
          <p className="mt-4 text-muted-foreground">{description}</p>
          <div className="mt-8 space-y-6 text-[15px] leading-relaxed text-foreground/90">
            {children}
          </div>
          <p className="mt-10">
            <Link href="/" className="text-sm font-medium text-primary hover:underline">
              Return to {siteConfig.name}
            </Link>
          </p>
        </article>
      </main>
    </div>
  );
}

export function legalWebPageJsonLd(opts: {
  name: string;
  description: string;
  path: string;
}) {
  return {
    "@context": "https://schema.org",
    "@type": "WebPage",
    name: opts.name,
    description: opts.description,
    url: `${siteConfig.url}${opts.path}`,
    isPartOf: {
      "@type": "WebSite",
      name: siteConfig.name,
      url: siteConfig.url,
    },
    inLanguage: "en-GB",
    dateModified: siteConfig.legalLastUpdated,
  };
}
