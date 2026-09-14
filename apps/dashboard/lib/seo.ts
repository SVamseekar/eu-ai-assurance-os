import type { Metadata } from "next";

import { siteConfig } from "@/lib/site-config";

export type MarketingCrumb = {
  name: string;
  path: string;
};

export function marketingMetadata({
  title,
  description,
  path,
  keywords,
  absoluteTitle,
}: {
  title: string;
  description: string;
  path: string;
  keywords?: string[];
  /** Use on the homepage so the layout title template is not appended. */
  absoluteTitle?: string;
}): Metadata {
  const url = path === "/" ? siteConfig.url : `${siteConfig.url}${path}`;
  const ogTitle = `${title} — ${siteConfig.name}`;
  return {
    title: absoluteTitle ? { absolute: absoluteTitle } : title,
    description,
    keywords,
    alternates: { canonical: path },
    openGraph: {
      title: ogTitle,
      description,
      url,
      type: "website",
    },
    twitter: {
      card: "summary_large_image",
      title: ogTitle,
      description,
    },
  };
}

export function webPageJsonLd({
  name,
  description,
  path,
  crumbs,
}: {
  name: string;
  description: string;
  path: string;
  crumbs?: MarketingCrumb[];
}) {
  const url = path === "/" ? siteConfig.url : `${siteConfig.url}${path}`;
  const graph: Record<string, unknown>[] = [
    {
      "@type": "WebPage",
      "@id": `${url}#webpage`,
      name,
      description,
      url,
      isPartOf: {
        "@type": "WebSite",
        name: siteConfig.name,
        url: siteConfig.url,
      },
      inLanguage: "en-GB",
      dateModified: siteConfig.legalLastUpdated,
    },
  ];

  if (crumbs?.length) {
    graph.push({
      "@type": "BreadcrumbList",
      itemListElement: crumbs.map((crumb, index) => ({
        "@type": "ListItem",
        position: index + 1,
        name: crumb.name,
        item: crumb.path === "/" ? siteConfig.url : `${siteConfig.url}${crumb.path}`,
      })),
    });
  }

  return {
    "@context": "https://schema.org",
    "@graph": graph,
  };
}
