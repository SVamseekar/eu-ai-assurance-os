import Link from "next/link";
import { ArrowRight } from "lucide-react";

import { formatLimit, formatPrice, PLANS } from "@/lib/pricing";
import { cn } from "@/lib/utils";

/** Home-page summary of the self-serve plans; the full table lives on /pricing. */
export function PricingTeaser() {
  const plans = PLANS.filter((p) => p.code !== "ENTERPRISE");
  return (
    <ul className="grid gap-5 md:grid-cols-3">
      {plans.map((plan) => {
        const featured = plan.code === "TEAM";
        return (
          <li
            key={plan.code}
            className={cn(
              "flex flex-col rounded-2xl border bg-white p-6",
              featured ? "border-brand ring-1 ring-brand" : "border-line",
            )}
          >
            <h3 className="text-lg font-bold text-ink">{plan.name}</h3>
            <p className="mt-3 flex items-baseline gap-1.5">
              <span className="text-4xl font-bold tracking-tight text-ink">{formatPrice(plan.monthly)}</span>
              {plan.monthly ? <span className="text-sm text-ink-muted">per month</span> : null}
            </p>
            <p className="mt-3 flex-1 text-sm text-ink-muted">
              {formatLimit(plan.gatedSystems)} gated AI system{plan.gatedSystems === 1 ? "" : "s"} ·{" "}
              {formatLimit(plan.editors)} editors ·{" "}
              {plan.gateRunsPerMonth === null ? "unlimited gate runs" : `${plan.gateRunsPerMonth} gate runs / month`}
            </p>
            <Link
              href={plan.code === "FREE" ? "/signup" : `/signup?plan=${plan.code}&interval=monthly`}
              className={cn(
                "mt-6 inline-flex h-11 items-center justify-center gap-2 rounded-lg text-sm font-semibold",
                featured ? "bg-brand text-white hover:bg-brand-hover" : "border border-brand/40 text-brand hover:bg-brand-soft",
              )}
            >
              {plan.code === "FREE" ? "Start free" : "Start free trial"}
              <ArrowRight className="h-4 w-4" aria-hidden="true" />
            </Link>
          </li>
        );
      })}
    </ul>
  );
}
