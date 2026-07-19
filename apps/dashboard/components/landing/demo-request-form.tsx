"use client";

import Link from "next/link";
import { useMemo, useState, type FormEvent } from "react";
import { AlertCircle, CheckCircle2, Loader2 } from "lucide-react";

import { Button } from "@/components/ui/button";
import {
  AI_SYSTEMS_COUNTS,
  COMPANY_SIZES,
  CURRENT_TOOLING,
  EU_COUNTRIES,
  HIGH_RISK_OPTIONS,
  INDUSTRIES,
  PRIMARY_INTERESTS,
  REFERRAL_SOURCES,
  TIMELINES,
} from "@/lib/demo-form-options";
import { submitDemoRequest, type DemoRequestInput } from "@/lib/demo-request";
import { siteConfig } from "@/lib/site-config";
import { cn } from "@/lib/utils";

const EMPTY_FORM: DemoRequestInput = {
  firstName: "",
  lastName: "",
  workEmail: "",
  phone: "",
  jobTitle: "",
  companyName: "",
  companyWebsite: "",
  companySize: "",
  industry: "",
  country: "",
  headquartersCity: "",
  aiSystemsCount: "",
  highRiskExposure: "",
  currentTooling: "",
  primaryInterests: [],
  timeline: "",
  referralSource: "",
  message: "",
  marketingConsent: false,
  privacyConsent: false,
  website: "",
  formStartedAt: Date.now(),
};

const fieldClass =
  "mt-1.5 w-full rounded-lg border border-border bg-background px-3 py-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50";
const labelClass = "block text-sm font-medium text-foreground";
const legendClass =
  "mb-3 text-xs font-semibold uppercase tracking-wider text-muted-foreground";

export function DemoRequestForm() {
  const [form, setForm] = useState<DemoRequestInput>(() => ({
    ...EMPTY_FORM,
    formStartedAt: Date.now(),
  }));
  const [status, setStatus] = useState<"idle" | "submitting" | "success" | "error">(
    "idle",
  );
  const [errorMessage, setErrorMessage] = useState("");

  const interestSet = useMemo(() => new Set(form.primaryInterests), [form.primaryInterests]);

  const updateField = <K extends keyof DemoRequestInput>(
    key: K,
    value: DemoRequestInput[K],
  ) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const toggleInterest = (interest: string) => {
    setForm((prev) => {
      const next = new Set(prev.primaryInterests);
      if (next.has(interest)) next.delete(interest);
      else next.add(interest);
      return { ...prev, primaryInterests: [...next] };
    });
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setStatus("submitting");
    setErrorMessage("");

    try {
      await submitDemoRequest(form);
      setStatus("success");
    } catch (error) {
      setStatus("error");
      setErrorMessage(
        error instanceof Error ? error.message : "Something went wrong",
      );
    }
  };

  if (status === "success") {
    return (
      <div
        className="rounded-xl border border-border bg-card p-8 text-center shadow-sm"
        role="status"
      >
        <CheckCircle2 className="mx-auto h-8 w-8 text-emerald-600" aria-hidden />
        <h2 className="mt-4 font-heading text-xl font-semibold">Request received</h2>
        <p className="mt-2 text-sm text-muted-foreground">
          Thanks — we&apos;ll review your details and get back to you at{" "}
          <strong className="text-foreground">{form.workEmail}</strong> within one
          business day.
        </p>
        <Button className="mt-6" render={<Link href="/" />}>
          Back to home
        </Button>
      </div>
    );
  }

  return (
    <form
      className="relative rounded-xl border border-border bg-card p-6 shadow-sm sm:p-8"
      onSubmit={handleSubmit}
      noValidate
    >
      <p className="text-sm text-muted-foreground">
        Tell us about your AI systems and governance goals. The more context you
        share, the better we can tailor the walkthrough.
      </p>

      {/* Honeypot — hidden from users */}
      <div className="absolute -left-[9999px] top-auto h-0 w-0 overflow-hidden" aria-hidden>
        <label>
          Website
          <input
            type="text"
            name="website"
            tabIndex={-1}
            autoComplete="off"
            value={form.website}
            onChange={(e) => updateField("website", e.target.value)}
          />
        </label>
      </div>

      <fieldset className="mt-8">
        <legend className={legendClass}>Contact</legend>
        <div className="grid gap-4 sm:grid-cols-2">
          <label className={labelClass}>
            First name <span className="text-destructive">*</span>
            <input
              required
              autoComplete="given-name"
              className={fieldClass}
              value={form.firstName}
              onChange={(e) => updateField("firstName", e.target.value)}
            />
          </label>
          <label className={labelClass}>
            Last name <span className="text-destructive">*</span>
            <input
              required
              autoComplete="family-name"
              className={fieldClass}
              value={form.lastName}
              onChange={(e) => updateField("lastName", e.target.value)}
            />
          </label>
          <label className={labelClass}>
            Work email <span className="text-destructive">*</span>
            <input
              required
              type="email"
              autoComplete="email"
              className={fieldClass}
              value={form.workEmail}
              onChange={(e) => updateField("workEmail", e.target.value)}
            />
          </label>
          <label className={labelClass}>
            Phone
            <input
              type="tel"
              autoComplete="tel"
              placeholder="+49 30 1234567"
              className={fieldClass}
              value={form.phone}
              onChange={(e) => updateField("phone", e.target.value)}
            />
          </label>
          <label className={cn(labelClass, "sm:col-span-2")}>
            Job title <span className="text-destructive">*</span>
            <input
              required
              autoComplete="organization-title"
              placeholder="e.g. Head of AI Governance"
              className={fieldClass}
              value={form.jobTitle}
              onChange={(e) => updateField("jobTitle", e.target.value)}
            />
          </label>
        </div>
      </fieldset>

      <fieldset className="mt-8">
        <legend className={legendClass}>Organisation</legend>
        <div className="grid gap-4 sm:grid-cols-2">
          <label className={labelClass}>
            Company name <span className="text-destructive">*</span>
            <input
              required
              autoComplete="organization"
              className={fieldClass}
              value={form.companyName}
              onChange={(e) => updateField("companyName", e.target.value)}
            />
          </label>
          <label className={labelClass}>
            Company website
            <input
              type="url"
              placeholder="https://"
              className={fieldClass}
              value={form.companyWebsite}
              onChange={(e) => updateField("companyWebsite", e.target.value)}
            />
          </label>
          <label className={labelClass}>
            Company size <span className="text-destructive">*</span>
            <select
              required
              className={fieldClass}
              value={form.companySize}
              onChange={(e) => updateField("companySize", e.target.value)}
            >
              <option value="">Select…</option>
              {COMPANY_SIZES.map((size) => (
                <option key={size} value={size}>
                  {size}
                </option>
              ))}
            </select>
          </label>
          <label className={labelClass}>
            Industry <span className="text-destructive">*</span>
            <select
              required
              className={fieldClass}
              value={form.industry}
              onChange={(e) => updateField("industry", e.target.value)}
            >
              <option value="">Select…</option>
              {INDUSTRIES.map((industry) => (
                <option key={industry} value={industry}>
                  {industry}
                </option>
              ))}
            </select>
          </label>
          <label className={labelClass}>
            Country <span className="text-destructive">*</span>
            <select
              required
              className={fieldClass}
              value={form.country}
              onChange={(e) => updateField("country", e.target.value)}
            >
              <option value="">Select…</option>
              {EU_COUNTRIES.map((country) => (
                <option key={country} value={country}>
                  {country}
                </option>
              ))}
            </select>
          </label>
          <label className={labelClass}>
            Headquarters city
            <input
              autoComplete="address-level2"
              className={fieldClass}
              value={form.headquartersCity}
              onChange={(e) => updateField("headquartersCity", e.target.value)}
            />
          </label>
        </div>
      </fieldset>

      <fieldset className="mt-8">
        <legend className={legendClass}>EU AI Act context</legend>
        <div className="grid gap-4 sm:grid-cols-2">
          <label className={labelClass}>
            AI systems in production / planned{" "}
            <span className="text-destructive">*</span>
            <select
              required
              className={fieldClass}
              value={form.aiSystemsCount}
              onChange={(e) => updateField("aiSystemsCount", e.target.value)}
            >
              <option value="">Select…</option>
              {AI_SYSTEMS_COUNTS.map((item) => (
                <option key={item} value={item}>
                  {item}
                </option>
              ))}
            </select>
          </label>
          <label className={labelClass}>
            High-risk exposure <span className="text-destructive">*</span>
            <select
              required
              className={fieldClass}
              value={form.highRiskExposure}
              onChange={(e) => updateField("highRiskExposure", e.target.value)}
            >
              <option value="">Select…</option>
              {HIGH_RISK_OPTIONS.map((item) => (
                <option key={item} value={item}>
                  {item}
                </option>
              ))}
            </select>
          </label>
          <label className={cn(labelClass, "sm:col-span-2")}>
            Current tooling <span className="text-destructive">*</span>
            <select
              required
              className={fieldClass}
              value={form.currentTooling}
              onChange={(e) => updateField("currentTooling", e.target.value)}
            >
              <option value="">Select…</option>
              {CURRENT_TOOLING.map((item) => (
                <option key={item} value={item}>
                  {item}
                </option>
              ))}
            </select>
          </label>
          <label className={labelClass}>
            Timeline <span className="text-destructive">*</span>
            <select
              required
              className={fieldClass}
              value={form.timeline}
              onChange={(e) => updateField("timeline", e.target.value)}
            >
              <option value="">Select…</option>
              {TIMELINES.map((item) => (
                <option key={item} value={item}>
                  {item}
                </option>
              ))}
            </select>
          </label>
          <label className={labelClass}>
            How did you hear about us? <span className="text-destructive">*</span>
            <select
              required
              className={fieldClass}
              value={form.referralSource}
              onChange={(e) => updateField("referralSource", e.target.value)}
            >
              <option value="">Select…</option>
              {REFERRAL_SOURCES.map((item) => (
                <option key={item} value={item}>
                  {item}
                </option>
              ))}
            </select>
          </label>
        </div>

        <div className="mt-4">
          <p className={labelClass}>
            Primary interests <span className="text-destructive">*</span>
          </p>
          <p className="mt-1 text-xs text-muted-foreground">
            Select at least one.
          </p>
          <ul className="mt-3 grid gap-2 sm:grid-cols-2">
            {PRIMARY_INTERESTS.map((interest) => (
              <li key={interest}>
                <label className="flex cursor-pointer items-start gap-2 rounded-lg border border-border px-3 py-2 text-sm hover:bg-muted/50">
                  <input
                    type="checkbox"
                    className="mt-0.5"
                    checked={interestSet.has(interest)}
                    onChange={() => toggleInterest(interest)}
                  />
                  <span>{interest}</span>
                </label>
              </li>
            ))}
          </ul>
        </div>

        <label className={cn(labelClass, "mt-4 block")}>
          Anything else we should know?
          <textarea
            rows={4}
            className={cn(fieldClass, "resize-y")}
            value={form.message}
            onChange={(e) => updateField("message", e.target.value)}
          />
        </label>
      </fieldset>

      <fieldset className="mt-8 space-y-3">
        <legend className={legendClass}>Consent</legend>
        <label className="flex items-start gap-2 text-sm">
          <input
            type="checkbox"
            required
            className="mt-0.5"
            checked={form.privacyConsent}
            onChange={(e) => updateField("privacyConsent", e.target.checked)}
          />
          <span>
            I agree to the processing of my details as described in the{" "}
            <Link href="/privacy" className="text-primary hover:underline">
              Privacy Policy
            </Link>{" "}
            <span className="text-destructive">*</span>
          </span>
        </label>
        <label className="flex items-start gap-2 text-sm text-muted-foreground">
          <input
            type="checkbox"
            className="mt-0.5"
            checked={form.marketingConsent}
            onChange={(e) => updateField("marketingConsent", e.target.checked)}
          />
          <span>
            Optional: send product updates about {siteConfig.shortName} (you can
            unsubscribe anytime).
          </span>
        </label>
      </fieldset>

      {status === "error" && (
        <div
          className="mt-6 flex items-start gap-2 rounded-lg border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive"
          role="alert"
        >
          <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" aria-hidden />
          <span>{errorMessage}</span>
        </div>
      )}

      <div className="mt-8 flex flex-wrap items-center gap-3">
        <Button type="submit" size="lg" disabled={status === "submitting"}>
          {status === "submitting" ? (
            <>
              <Loader2 className="animate-spin" data-icon="inline-start" />
              Sending…
            </>
          ) : (
            "Request demo"
          )}
        </Button>
        <p className="text-xs text-muted-foreground">
          Prefer email?{" "}
          <a
            href={`mailto:${siteConfig.supportEmail}`}
            className="text-primary hover:underline"
          >
            {siteConfig.supportEmail}
          </a>
        </p>
      </div>
    </form>
  );
}
