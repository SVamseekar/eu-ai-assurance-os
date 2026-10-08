/**
 * Opt-in consent for Google Analytics. Nothing from Google loads until the visitor accepts;
 * Cloudflare Web Analytics is cookieless and runs regardless.
 */
export type AnalyticsConsent = "granted" | "denied";

export const CONSENT_STORAGE_KEY = "eu-ai-analytics-consent";

/** Fired on window to reopen the banner, e.g. from the privacy page. */
export const OPEN_CONSENT_EVENT = "eu-ai:open-cookie-settings";

export function parseConsent(value: string | null | undefined): AnalyticsConsent | null {
  return value === "granted" || value === "denied" ? value : null;
}

export function readConsent(): AnalyticsConsent | null {
  try {
    return parseConsent(window.localStorage.getItem(CONSENT_STORAGE_KEY));
  } catch {
    return null;
  }
}

export function writeConsent(consent: AnalyticsConsent) {
  try {
    window.localStorage.setItem(CONSENT_STORAGE_KEY, consent);
  } catch {
    // Storage blocked: the choice holds for this page view only.
  }
}

/** GA4 sets `_ga` and `_ga_<container>` on the registrable domain; clear them on every parent domain. */
export function gaCookieNames(cookieHeader: string): string[] {
  return cookieHeader
    .split(";")
    .map((c) => c.split("=")[0].trim())
    .filter((name) => name === "_ga" || name.startsWith("_ga_") || name === "_gid");
}

export function clearGaCookies() {
  const parts = window.location.hostname.split(".");
  const domains = parts.map((_, i) => parts.slice(i).join(".")).filter((d) => d.includes("."));
  for (const name of gaCookieNames(document.cookie)) {
    document.cookie = `${name}=; Max-Age=0; path=/`;
    for (const domain of domains) document.cookie = `${name}=; Max-Age=0; path=/; domain=.${domain}`;
  }
}
