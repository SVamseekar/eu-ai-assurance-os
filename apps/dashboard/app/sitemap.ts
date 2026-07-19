import type { MetadataRoute } from "next";

import { publicRoutes, siteConfig } from "@/lib/site-config";

export default function sitemap(): MetadataRoute.Sitemap {
  return publicRoutes.map((route) => ({
    url: route.path === "/" ? siteConfig.url : `${siteConfig.url}${route.path}`,
    lastModified: new Date(),
    changeFrequency: route.changeFrequency,
    priority: route.priority,
  }));
}
