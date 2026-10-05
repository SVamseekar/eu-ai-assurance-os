"use client";

import Link from "next/link";
import { useState, type ReactNode } from "react";
import { ArrowRight, Check } from "lucide-react";

import { mkButton } from "@/components/marketing/primitives";
import { formatLimit, formatPrice, PLANS, type PlanDisplay } from "@/lib/pricing";
import { cn } from "@/lib/utils";

type Interval = "monthly" | "yearly";

const blurb: Record<PlanDisplay["code"], string> = {
  FREE: "For one system and a small team",
  TEAM: "For growing teams",
  BUSINESS: "For several products and teams",
  ENTERPRISE: "For larger organisations",
};

function cta(plan: PlanDisplay, interval: Interval) {
  switch (plan.code) {
    case "FREE":
      return { href: "/signup", label: "Get started", style: mkButton.outline };
    case "TEAM":
      return { href: `/signup?plan=TEAM&interval=${interval}`, label: "Start free trial", style: mkButton.primary };
    case "BUSINESS":
      return { href: `/signup?plan=BUSINESS&interval=${interval}`, label: "Start free trial", style: mkButton.outline };
    case "ENTERPRISE":
      return { href: "/request-demo", label: "Talk to us", style: mkButton.outline };
  }
}

/** Storyboard frame 10. Prices, limits and "coming soon" flags all come from lib/pricing.ts. */
export function PricingTable({ plans = PLANS, header }: { plans?: PlanDisplay[]; header?: ReactNode }) {
  const [interval, setBillingInterval] = useState<Interval>("monthly");

  return (
    <div>
      <div className="flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
        {header}
        <div role="group" aria-label="Billing interval" className="inline-flex shrink-0 items-center gap-1 self-start rounded-xl border border-line bg-mist p-1 lg:self-end">
          {(["monthly", "yearly"] as const).map((value) => (
            <button
              key={value}
              type="button"
              aria-pressed={interval === value}
              onClick={() => setBillingInterval(value)}
              className={cn(
                "rounded-lg px-4 py-2 text-sm font-semibold transition-colors",
                interval === value ? "bg-brand text-white shadow-sm" : "text-ink-muted hover:text-ink",
              )}
            >
              {value === "monthly" ? "Monthly" : "Yearly"}
            </button>
          ))}
          <span className="ml-1 rounded-lg bg-pass-soft px-2.5 py-1.5 text-xs font-semibold text-pass">2 months free</span>
        </div>
      </div>

      <ul className="mt-10 grid gap-5 sm:grid-cols-2 xl:grid-cols-4">
        {plans.map((plan) => {
          const featured = plan.code === "TEAM";
          const amount = interval === "monthly" ? plan.monthly : plan.yearly;
          const action = cta(plan, interval);
          const limits = [
            `${formatLimit(plan.gatedSystems)} gated AI system${plan.gatedSystems === 1 ? "" : "s"}`,
            `${formatLimit(plan.editors)} editors · unlimited viewers`,
            plan.gateRunsPerMonth === null ? "Unlimited gate runs" : `${plan.gateRunsPerMonth} gate runs / month`,
          ];
          return (
            <li
              key={plan.code}
              className={cn(
                "relative flex flex-col rounded-2xl border bg-white p-6",
                featured ? "border-brand shadow-xl shadow-brand/10 ring-1 ring-brand" : "border-line",
              )}
            >
              {featured ? (
                <span className="absolute -top-3 left-6 rounded-full bg-brand px-3 py-1 text-[11px] font-semibold uppercase tracking-wide text-white">
                  Most popular
                </span>
              ) : null}
              <h2 className="text-lg font-bold text-ink">{plan.name}</h2>
              <p className="text-sm text-ink-muted">{blurb[plan.code]}</p>
              <p className="mt-5 flex items-baseline gap-1.5">
                <span className="text-4xl font-bold tracking-tight text-ink">{formatPrice(amount)}</span>
                {amount !== null && amount > 0 ? (
                  <span className="text-sm text-ink-muted">per {interval === "monthly" ? "month" : "year"}</span>
                ) : null}
              </p>
              <p className="mt-1 text-xs text-ink-muted">
                {plan.code === "FREE" ? "No card required" : plan.code === "ENTERPRISE" ? "Contact us" : "USD, excl. tax"}
              </p>
              <Link href={action.href} className={cn(action.style, "mt-6 w-full")}>
                {action.label}
                <ArrowRight className="h-4 w-4" aria-hidden="true" />
              </Link>
              <ul className="mt-6 space-y-2.5 border-t border-line pt-6 text-sm">
                {[
                  ...(plan.code === "ENTERPRISE" ? ["Unlimited systems and editors", "Order form and MSA", "Onboarding assistance"] : limits),
                  ...plan.features.filter((f) => f.available).map((f) => f.label),
                ].map((item) => (
                  <li key={item} className="flex items-start gap-2 text-ink/85">
                    <Check className="mt-0.5 h-4 w-4 shrink-0 text-pass" aria-hidden="true" />
                    {item}
                  </li>
                ))}
              </ul>
              {plan.features.some((f) => !f.available) ? (
                <div className="mt-5">
                  <p className="text-[11px] font-semibold uppercase tracking-wide text-ink-muted">Coming soon</p>
                  <ul className="mt-2 space-y-1.5 text-xs text-ink-muted">
                    {plan.features
                      .filter((f) => !f.available)
                      .map((f) => (
                        <li key={f.label}>{f.label.replace(/\s*\(coming soon\)$/i, "")}</li>
                      ))}
                  </ul>
                </div>
              ) : null}
            </li>
          );
        })}
      </ul>
    </div>
  );
}
