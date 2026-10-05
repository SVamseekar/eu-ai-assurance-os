import type { MetadataRoute } from "next";

import { COMPARE_PAGES } from "@/content/compare";
import { PROVISIONS } from "@/lib/eu-ai-act-provisions";
import { publicRoutes, siteConfig } from "@/lib/site-config";

export default function sitemap(): MetadataRoute.Sitemap {
  const routes = publicRoutes.map((route) => ({
    url: route.path === "/" ? siteConfig.url : `${siteConfig.url}${route.path}`,
    lastModified: new Date(),
    changeFrequency: route.changeFrequency,
    priority: route.priority,
  }));
  const compare = COMPARE_PAGES.map((page) => ({
    url: `${siteConfig.url}/compare/${page.slug}`,
    lastModified: new Date(page.retrieved),
    changeFrequency: "monthly" as const,
    priority: 0.6,
  }));
  const provisions = PROVISIONS.map((p) => ({
    url: `${siteConfig.url}/eu-ai-act/${p.slug}`,
    lastModified: new Date(),
    changeFrequency: "monthly" as const,
    priority: 0.7,
  }));
  return [...routes, ...compare, ...provisions];
}
