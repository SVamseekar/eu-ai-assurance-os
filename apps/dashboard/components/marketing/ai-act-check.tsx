"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { AlertTriangle, ArrowRight, CircleHelp, Loader2 } from "lucide-react";

import { DemoButton } from "@/components/marketing/demo-button";
import { CheckDot, mkButton } from "@/components/marketing/primitives";
import {
  relevantDeadlines,
  riskClassCopy,
  toAnswers,
  type CheckObligation,
  type CheckResult,
  type Questionnaire,
} from "@/lib/ai-act-check";
import { daysUntil, deadlineStatus, formatDeadlineDate } from "@/lib/deadlines";
import { cn } from "@/lib/utils";

const fieldClass =
  "mt-1.5 min-h-11 w-full rounded-lg border border-line bg-white px-3 py-2 text-sm text-ink outline-none focus-visible:border-brand focus-visible:ring-3 focus-visible:ring-brand/20";

/** Free applicability check: questionnaire from the public API, result rendered in place. Nothing is stored. */
export function AiActCheck() {
  const [questionnaire, setQuestionnaire] = useState<Questionnaire | null>(null);
  const [loadError, setLoadError] = useState(false);
  const [form, setForm] = useState<Record<string, string>>({});
  const [result, setResult] = useState<CheckResult | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    fetch("/api/public/determination")
      .then((r) => (r.ok ? r.json() : Promise.reject(new Error("unavailable"))))
      .then((q: Questionnaire) => {
        if (!cancelled) setQuestionnaire(q);
      })
      .catch(() => {
        if (!cancelled) setLoadError(true);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!event.currentTarget.reportValidity()) return;
    setBusy(true);
    setError(null);
    try {
      const res = await fetch("/api/public/determination", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ answers: toAnswers(form) }),
      });
      const body = await res.json().catch(() => ({}));
      if (!res.ok) throw new Error(typeof body.error === "string" ? body.error : "The check is unavailable right now.");
      setResult(body as CheckResult);
      requestAnimationFrame(() => document.getElementById("check-result")?.scrollIntoView({ behavior: "smooth" }));
    } catch (e) {
      setError(e instanceof Error ? e.message : "The check is unavailable right now.");
    } finally {
      setBusy(false);
    }
  }

  if (loadError) {
    return (
      <p role="alert" className="rounded-xl border border-review/30 bg-review-soft p-4 text-sm text-ink">
        The check is unavailable right now. Try again in a few minutes, or{" "}
        <Link href="/tools/ai-act-deadlines" className="font-semibold text-brand hover:underline">
          see the deadline calendar
        </Link>
        .
      </p>
    );
  }
  if (!questionnaire) {
    return (
      <p className="flex items-center gap-2 text-sm text-ink-muted">
        <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" /> Loading questions…
      </p>
    );
  }

  return (
    <div className="grid gap-10 lg:grid-cols-[1.1fr_1fr] lg:items-start">
      <form onSubmit={submit} className="space-y-5 rounded-2xl border border-line bg-white p-6 shadow-sm sm:p-8">
        {questionnaire.questions.map((q) => (
          <label key={q.id} className="block text-sm font-semibold text-ink">
            {q.label}
            {q.required ? <span className="text-blocked" aria-hidden="true"> *</span> : null}
            <span className="mt-0.5 block text-xs font-normal text-ink-muted">{q.help}</span>
            <select
              name={q.id}
              required={q.required}
              className={fieldClass}
              value={form[q.id] ?? ""}
              onChange={(e) => setForm((f) => ({ ...f, [q.id]: e.target.value }))}
            >
              <option value="">Select…</option>
              {q.options.map((o) => (
                <option key={o.value} value={o.value}>
                  {o.label.charAt(0).toUpperCase() + o.label.slice(1)}
                </option>
              ))}
            </select>
          </label>
        ))}
        {error ? (
          <p role="alert" className="flex items-start gap-2 rounded-lg bg-blocked-soft px-3 py-2 text-sm text-blocked">
            <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />
            {error}
          </p>
        ) : null}
        <button type="submit" className={cn(mkButton.primary, "w-full")} disabled={busy}>
          {busy ? <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" /> : null}
          {busy ? "Checking…" : "Check my AI system"}
        </button>
        <p className="text-xs text-ink-muted">No sign-up. Your answers are not stored.</p>
      </form>

      <div id="check-result" className="scroll-mt-28 lg:sticky lg:top-28">
        {result ? <CheckResultPanel result={result} /> : <ResultPlaceholder />}
      </div>
    </div>
  );
}

function ResultPlaceholder() {
  return (
    <div className="rounded-2xl border border-dashed border-line bg-mist p-8 text-sm text-ink-muted">
      <CircleHelp className="h-6 w-6 text-brand" aria-hidden="true" />
      <p className="mt-3 font-semibold text-ink">Your result appears here</p>
      <p className="mt-1">
        A suggested risk class, the obligations that may apply with their legal references, and the dates that matter.
      </p>
    </div>
  );
}

function CheckResultPanel({ result }: { result: CheckResult }) {
  const cls = result.riskSuggestion.suggestedRiskClass;
  const copy = riskClassCopy[cls];
  const applicable = result.obligations.filter((o) => o.applicability === "APPLICABLE");
  const uncertain = result.obligations.filter((o) => o.applicability === "UNCERTAIN");
  const dates = relevantDeadlines(result);
  const today = new Date();
  return (
    <section aria-live="polite" className="space-y-5 rounded-2xl border border-line bg-white p-6 shadow-sm sm:p-8">
      <div>
        <p className="text-xs font-semibold uppercase tracking-wide text-ink-muted">Suggested risk class</p>
        <p
          className={cn(
            "mt-1 text-2xl font-bold tracking-tight",
            cls === "PROHIBITED" ? "text-blocked" : cls === "HIGH" ? "text-review" : "text-pass",
          )}
        >
          {copy.label}
        </p>
        <p className="mt-1 text-sm text-ink-muted">{copy.summary}</p>
      </div>

      <ObligationList title="Likely applies" items={applicable} tone="pass" />
      <ObligationList title="Needs legal input" items={uncertain} tone="review" />

      {dates.length ? (
        <div>
          <p className="text-sm font-semibold text-ink">Dates that matter</p>
          <ul className="mt-2 space-y-1.5 text-sm">
            {dates.map((d) => (
              <li key={d.id} className="flex flex-wrap justify-between gap-2">
                <span className="text-ink/85">{d.label}</span>
                <span className="text-ink-muted">
                  {formatDeadlineDate(d.date)} ·{" "}
                  {deadlineStatus(d, today) === "applies-now" ? "applies now" : `in ${daysUntil(d, today)} days`}
                </span>
              </li>
            ))}
          </ul>
        </div>
      ) : null}

      <p className="rounded-lg bg-mist p-3 text-xs text-ink-muted">{result.disclaimer}</p>

      <div className="flex flex-col gap-3 border-t border-line pt-5">
        <Link href="/signup" className={cn(mkButton.primary, "w-full")}>
          Save this and gate it in CI — Start free
          <ArrowRight className="h-4 w-4" aria-hidden="true" />
        </Link>
        <div className="[&_button]:w-full">
          <DemoButton variant="outline" />
        </div>
        <p className="text-center text-xs text-ink-muted">See it on a real system in the read-only demo.</p>
      </div>
    </section>
  );
}

function ObligationList({ title, items, tone }: { title: string; items: CheckObligation[]; tone: "pass" | "review" }) {
  if (!items.length) return null;
  return (
    <div>
      <p className="text-sm font-semibold text-ink">
        {title} <span className="font-normal text-ink-muted">({items.length})</span>
      </p>
      <ul className="mt-2 space-y-2">
        {items.map((o) => (
          <li key={o.ruleCode} className="flex gap-2.5 text-sm">
            {tone === "pass" ? (
              <CheckDot className="mt-0.5 h-4 w-4 [&>svg]:h-2.5 [&>svg]:w-2.5" />
            ) : (
              <CircleHelp className="mt-0.5 h-4 w-4 shrink-0 text-review" aria-hidden="true" />
            )}
            <span>
              <span className="text-ink">{o.title}</span>
              {o.legalRefs ? <span className="block text-xs text-ink-muted">{o.legalRefs}</span> : null}
            </span>
          </li>
        ))}
      </ul>
    </div>
  );
}
