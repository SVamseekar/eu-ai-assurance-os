import { CodeTabs } from "@/components/marketing/code-tabs";
import { Check, Clock3, X } from "lucide-react";

import { ProductDetailPage } from "@/components/marketing/product-detail";
import { exitCodes, gateSnippets } from "@/lib/gate-snippets";
import { marketingMetadata } from "@/lib/seo";

const path = "/product/release-gate";
const description =
  "Simple API. Clear exit codes. Works with GitHub Actions, GitLab CI and any CI/CD system.";

export const metadata = marketingMetadata({
  title: "Release gate",
  description,
  path,
  keywords: ["AI release gate", "CI/CD AI governance", "GitHub Actions AI gate", "fail-closed"],
});

export default function ReleaseGatePage() {
  return (
    <ProductDetailPage
      tone="dark"
      path={path}
      name="Release gate"
      title="Enforce assurance in your pipeline."
      description={description}
      visual={<CodeTabs tabs={gateSnippets} compact />}
      aside={<ExitCodesCard />}
      primary={{ href: "/signup", label: "Get an API key" }}
      pointsTitle="Fail closed, explain why."
      points={[
        { title: "One read-only call", body: "GET the release gate with an API key and the system ID. Nothing to install in your runner beyond curl." },
        { title: "Fails closed", body: "If the decision is missing or unreadable, the gate returns BLOCKED, never PASS." },
        { title: "Blockers you can act on", body: "The response lists every blocker, so the failing job tells the engineer what to fix." },
        { title: "REVIEW is your call", body: "Treat REVIEW as a warning or a stop. BLOCKED always stops the job." },
        { title: "Scoped API keys", body: "CI keys act with their creator's permissions and cannot create or revoke other keys." },
        { title: "Open-source scan", body: "The same evidence can be checked locally with evgraph-cli, outside the platform." },
      ]}
    />
  );
}

const exitIcon = {
  PASS: { icon: Check, className: "bg-pass text-white", text: "text-pass" },
  REVIEW: { icon: Clock3, className: "bg-review text-white", text: "text-review" },
  BLOCKED: { icon: X, className: "bg-blocked text-white", text: "text-blocked" },
} as const;

/** Storyboard 09: exit-code legend beside the snippet. */
function ExitCodesCard() {
  return (
    <div className="flex w-full flex-col rounded-2xl bg-white p-6 text-ink shadow-xl shadow-black/20">
      <h2 className="font-semibold">Exit codes</h2>
      <ul className="mt-6 flex flex-1 flex-col justify-evenly gap-6">
        {exitCodes.map((e) => {
          const style = exitIcon[e.decision];
          return (
            <li key={e.decision} className="flex gap-3">
              <span className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-full ${style.className}`}>
                <style.icon className="h-5 w-5" strokeWidth={3} aria-hidden="true" />
              </span>
              <div>
                <p className={`font-bold tracking-tight ${style.text}`}>{e.decision}</p>
                <p className="text-xs font-medium text-ink-muted">Exit code {e.code}</p>
                <p className="mt-1 text-sm text-ink-muted">{e.detail}</p>
              </div>
            </li>
          );
        })}
      </ul>
    </div>
  );
}
