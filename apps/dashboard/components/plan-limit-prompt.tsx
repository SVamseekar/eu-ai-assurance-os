"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { PLAN_LIMIT_EVENT } from "@/lib/plan-limit";
import { billingBanner } from "@/lib/billing-view";

/** Trial/payment banner plus a dialog that opens whenever the API answers 402 (a plan limit). */
export function PlanLimitPrompt() {
  const [message, setMessage] = useState<string | null>(null);
  const billing = useQuery({ queryKey: ["billing"], queryFn: api.billing.get, retry: false, staleTime: 60_000 });

  useEffect(() => {
    const onLimit = (event: Event) => setMessage((event as CustomEvent<string>).detail ?? "Plan limit reached.");
    window.addEventListener(PLAN_LIMIT_EVENT, onLimit);
    return () => window.removeEventListener(PLAN_LIMIT_EVENT, onLimit);
  }, []);

  const banner = billing.data ? billingBanner(billing.data) : null;

  return (
    <>
      {banner && (
        <div role="status" className="mb-4 rounded-lg border border-primary/30 bg-primary/10 px-4 py-2.5 text-sm">
          {banner}{" "}
          <Link href="/settings#billing" className="font-medium underline underline-offset-4">
            Plans and billing
          </Link>
        </div>
      )}
      {message && (
        <div role="dialog" aria-modal="true" aria-label="Plan limit reached" className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="w-full max-w-md rounded-xl bg-background p-5 shadow-xl">
            <h2 className="text-base font-semibold">Plan limit reached</h2>
            <p className="mt-2 text-sm text-muted-foreground">{message}</p>
            <div className="mt-4 flex justify-end gap-2">
              <button type="button" className="rounded-md border border-border px-3 py-1.5 text-sm" onClick={() => setMessage(null)}>
                Not now
              </button>
              <Link
                href="/settings#billing"
                onClick={() => setMessage(null)}
                className="rounded-md bg-primary px-3 py-1.5 text-sm text-primary-foreground"
              >
                See plans
              </Link>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
