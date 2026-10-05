import type { ReactNode } from "react";

import { MarketingPageShell, PageIntro } from "@/components/landing/marketing-page-shell";
import { siteConfig } from "@/lib/site-config";

type LegalPageShellProps = {
  title: string;
  description: string;
  children: ReactNode;
};

export function LegalPageShell({ title, description, children }: LegalPageShellProps) {
  return (
    <MarketingPageShell>
      <PageIntro
        eyebrow="Legal"
        title={title}
        description={description}
      />
      <div className="mx-auto max-w-3xl px-4 py-12 sm:px-6 sm:py-16">
        <article className="prose-legal">
          <p className="text-sm text-ink-muted">Last updated: {siteConfig.legalLastUpdated}</p>
          <div className="mt-8 space-y-6 text-[15px] leading-relaxed text-ink/90 [&_a]:text-brand [&_h2]:text-xl [&_h2]:font-bold [&_h2]:tracking-tight [&_h2]:text-ink">
            {children}
          </div>
        </article>
      </div>
    </MarketingPageShell>
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
