"use client";

import Link from "next/link";
import { useEffect, useState } from "react";

import { Button } from "@/components/ui/button";
import {
  type AnalyticsConsent,
  clearGaCookies,
  OPEN_CONSENT_EVENT,
  readConsent,
  writeConsent,
} from "@/lib/analytics-consent";

declare global {
  interface Window {
    dataLayer?: unknown[];
    gtag?: (...args: unknown[]) => void;
  }
}

/** Injects gtag.js. Called only after the visitor accepts, so no Google request precedes consent. */
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
    allow_google_signals: false,
    allow_ad_personalization_signals: false,
  });
  const script = document.createElement("script");
  script.async = true;
  script.src = `https://www.googletagmanager.com/gtag/js?id=${encodeURIComponent(measurementId)}`;
  document.head.appendChild(script);
}

export function CookieConsent({ measurementId }: { measurementId: string }) {
  const [open, setOpen] = useState(false);

  useEffect(() => {
    const consent = readConsent();
    if (consent === "granted") loadGoogleAnalytics(measurementId);
    if (consent === null) setOpen(true);
    const reopen = () => setOpen(true);
    window.addEventListener(OPEN_CONSENT_EVENT, reopen);
    return () => window.removeEventListener(OPEN_CONSENT_EVENT, reopen);
  }, [measurementId]);

  const choose = (consent: AnalyticsConsent) => {
    const wasLoaded = Boolean(window.gtag);
    writeConsent(consent);
    setOpen(false);
    if (consent === "granted") {
      loadGoogleAnalytics(measurementId);
    } else if (wasLoaded) {
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
