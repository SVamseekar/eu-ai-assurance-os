"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";

import { Button } from "@/components/ui/button";
import {
  type AnalyticsConsent,
  analyticsLocation,
  clearGaCookies,
  isAnalyticsExcluded,
  OPEN_CONSENT_EVENT,
  readConsent,
  writeConsent,
} from "@/lib/analytics-consent";

declare global {
  interface Window {
    dataLayer?: unknown[];
    gtag?: (...args: unknown[]) => void;
    [gaDisable: `ga-disable-${string}`]: boolean | undefined;
  }
}

/**
 * Injects gtag.js. Called only after the visitor accepts, so no Google request precedes consent.
 * Page views are sent by hand with a query-free URL; automatic ones would carry the full address.
 */
function loadGoogleAnalytics(measurementId: string) {
  if (window.gtag) return;
  window.dataLayer = window.dataLayer || [];
  window.gtag = function gtag() {
    // gtag.js reads the `arguments` object, not an array.
    window.dataLayer!.push(arguments);
  };
  window.gtag("consent", "default", {
    analytics_storage: "granted",
    ad_storage: "denied",
    ad_user_data: "denied",
    ad_personalization: "denied",
  });
  window.gtag("js", new Date());
  window.gtag("config", measurementId, {
    send_page_view: false,
    allow_google_signals: false,
    allow_ad_personalization_signals: false,
  });
  const script = document.createElement("script");
  script.async = true;
  script.src = `https://www.googletagmanager.com/gtag/js?id=${encodeURIComponent(measurementId)}`;
  document.head.appendChild(script);
}

/** Last page sent to GA, so client-side navigations report a query-free referrer. */
let lastLocation = "";

/** Referrer origin only: the full referrer can carry tokens (e.g. arriving from a reset link). */
function referrerOrigin(): string {
  try {
    return document.referrer ? `${new URL(document.referrer).origin}/` : "";
  } catch {
    return "";
  }
}

/** Sends one page view for `pathname`, or silences gtag entirely on excluded routes. */
function trackPage(measurementId: string, pathname: string) {
  const excluded = isAnalyticsExcluded(pathname);
  // GA's documented opt-out flag: gtag.js drops every hit, automatic ones included, while it is true.
  window[`ga-disable-${measurementId}`] = excluded;
  if (excluded) {
    lastLocation = window.location.origin + "/";
    return;
  }
  loadGoogleAnalytics(measurementId);
  const page_location = analyticsLocation(window.location.origin, pathname);
  // gtag otherwise fills these from window.location and the previous URL, query strings included.
  const page_referrer = lastLocation || referrerOrigin();
  window.gtag!("set", { page_location, page_referrer });
  window.gtag!("event", "page_view", { page_location, page_referrer, page_title: document.title });
  lastLocation = page_location;
}

export function CookieConsent({ measurementId }: { measurementId: string }) {
  const pathname = usePathname();
  const [consent, setConsent] = useState<AnalyticsConsent | null | undefined>(undefined);
  const [reopened, setReopened] = useState(false);

  useEffect(() => {
    setConsent(readConsent());
    const reopen = () => setReopened(true);
    window.addEventListener(OPEN_CONSENT_EVENT, reopen);
    return () => window.removeEventListener(OPEN_CONSENT_EVENT, reopen);
  }, []);

  useEffect(() => {
    if (consent === "granted") trackPage(measurementId, pathname);
  }, [consent, measurementId, pathname]);

  const open = reopened || (consent === null && !isAnalyticsExcluded(pathname));

  const choose = (consent: AnalyticsConsent) => {
    const wasLoaded = Boolean(window.gtag);
    writeConsent(consent);
    setConsent(consent);
    setReopened(false);
    if (consent === "granted") return;
    if (wasLoaded) {
      // gtag.js cannot be unloaded: stop it writing cookies, clear them, and reload without it.
      window.gtag!("consent", "update", { analytics_storage: "denied" });
      clearGaCookies();
      window.location.reload();
    } else {
      clearGaCookies();
    }
  };

  if (!open) return null;

  return (
    <div
      role="dialog"
      aria-live="polite"
      aria-label="Cookie preferences"
      className="fixed inset-x-4 bottom-4 z-50 mx-auto max-w-xl rounded-xl border border-border bg-background p-4 text-sm shadow-lg"
    >
      <p className="text-foreground">
        We use Google Analytics cookies to understand how visitors use this site, only if you allow it. No advertising
        or cross-site tracking. See our <Link href="/privacy" className="underline underline-offset-2">privacy policy</Link>.
      </p>
      <div className="mt-3 flex justify-end gap-2">
        <Button variant="outline" onClick={() => choose("denied")}>
          Reject
        </Button>
        <Button onClick={() => choose("granted")}>Accept analytics</Button>
      </div>
    </div>
  );
}

/** Reopens the banner so visitors can change or withdraw consent. */
export function CookieSettingsButton() {
  return (
    <button
      type="button"
      className="underline underline-offset-2"
      onClick={() => window.dispatchEvent(new Event(OPEN_CONSENT_EVENT))}
    >
      Change cookie settings
    </button>
  );
}
