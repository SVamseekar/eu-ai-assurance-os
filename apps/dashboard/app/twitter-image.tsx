import { renderOgCard } from "@/components/marketing/og-card";
import { siteConfig } from "@/lib/site-config";

export const size = { width: 1200, height: 630 };
export const contentType = "image/png";
export const alt = `${siteConfig.shortName}: ${siteConfig.tagline}`;

export default function TwitterImage() {
  return renderOgCard();
}
