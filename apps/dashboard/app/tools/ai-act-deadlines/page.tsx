import Link from "next/link";
import { ExternalLink } from "lucide-react";

import { MarketingPageShell } from "@/components/landing/marketing-page-shell";
import { IcsDownload } from "@/components/marketing/ics-download";
import { Band, Container, PageHeader } from "@/components/marketing/primitives";
import { CtaBand } from "@/components/marketing/sections";
import { DEADLINES, daysUntil, deadlineStatus, formatDeadlineDate } from "@/lib/deadlines";
import { marketingMetadata, webPageJsonLd } from "@/lib/seo";
import { siteConfig } from "@/lib/site-config";
import { cn } from "@/lib/utils";

const title = "AI regulation deadline calendar";
const description =
  "Every EU AI Act date, plus Australia's automated-decision transparency duty and Colorado SB 26-189, with days remaining, official sources, and a calendar download.";
const path = "/tools/ai-act-deadlines";

// "Days until" is computed at build time; rebuild daily.
export const revalidate = 86400;

export const metadata = marketingMetadata({
  title,
  description,
  path,
  keywords: ["EU AI Act deadlines", "EU AI Act timeline", "Article 50 deadline", "Annex III date", "Colorado SB 26-189 date"],
});

export default function DeadlinesPage() {
  const today = new Date();
  return (
    <MarketingPageShell
      jsonLd={webPageJsonLd({
        name: `${title} — ${siteConfig.shortName}`,
        description,
        path,
        crumbs: [
          { name: "Home", path: "/" },
          { name: "Deadline calendar", path },
        ],
      })}
    >
      <Band tone="light" muted>
        <Container className="pt-10 pb-20 sm:pt-12">
          <PageHeader tone="light" eyebrow="Free tool" title="AI regulation deadlines." description={description}>
            <div className="mt-8">
              <IcsDownload />
            </div>
          </PageHeader>

          <div className="mt-10 overflow-x-auto rounded-2xl border border-line bg-white">
            <table className="w-full min-w-[44rem] text-left text-sm">
              <thead className="bg-mist text-ink-muted">
                <tr>
                  <th className="px-4 py-3 font-semibold">Date</th>
                  <th className="px-4 py-3 font-semibold">Law</th>
                  <th className="px-4 py-3 font-semibold">What applies</th>
                  <th className="px-4 py-3 font-semibold">Days until</th>
                  <th className="px-4 py-3 font-semibold">Source</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {DEADLINES.map((d) => {
                  const now = deadlineStatus(d, today) === "applies-now";
                  return (
                    <tr key={d.id} className="align-top">
                      <td className="px-4 py-3 font-semibold whitespace-nowrap text-ink">{formatDeadlineDate(d.date)}</td>
                      <td className="px-4 py-3 text-ink/85">{d.law}</td>
                      <td className="px-4 py-3 text-ink/85">{d.label}</td>
                      <td className="px-4 py-3">
                        <span
                          className={cn(
                            "rounded-full px-2 py-0.5 text-xs font-semibold whitespace-nowrap",
                            now ? "bg-review-soft text-review" : "bg-brand-soft text-brand",
                          )}
                        >
                          {now ? "Applies now" : `${daysUntil(d, today)} days`}
                        </span>
                      </td>
                      <td className="px-4 py-3">
                        <a
                          href={d.source.url}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="inline-flex items-center gap-1 text-xs font-medium text-brand hover:underline"
                        >
                          {d.source.title}
                          <ExternalLink className="h-3 w-3" aria-label="(opens in a new tab)" />
                        </a>
                        <span className="block text-[11px] text-ink-muted">Checked {formatDeadlineDate(d.source.retrieved)}</span>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
          <p className="mt-6 text-sm text-ink-muted">
            Not sure which dates apply to you?{" "}
            <Link href="/tools/ai-act-check" className="font-semibold text-brand hover:underline">
              Run the free applicability check
            </Link>
            . Dates are for planning, not legal advice.
          </p>
        </Container>
      </Band>
      <CtaBand />
    </MarketingPageShell>
  );
}
