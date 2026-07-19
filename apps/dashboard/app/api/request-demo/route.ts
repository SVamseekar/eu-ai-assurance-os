import { NextRequest, NextResponse } from "next/server";

import { siteConfig } from "@/lib/site-config";

export const runtime = "nodejs";

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const RATE_LIMIT_WINDOW_MS = 15 * 60 * 1000;
const RATE_LIMIT_MAX = 5;
const MIN_FILL_MS = 3000;

type RateEntry = { count: number; resetAt: number };

const globalForRate = globalThis as typeof globalThis & {
  __demoRateLimit?: Map<string, RateEntry>;
};

const ipHits: Map<string, RateEntry> =
  globalForRate.__demoRateLimit ?? new Map<string, RateEntry>();
globalForRate.__demoRateLimit = ipHits;

function asString(value: unknown): string {
  return typeof value === "string" ? value.trim() : "";
}

function asBoolean(value: unknown): boolean {
  return value === true || value === "true" || value === 1 || value === "1";
}

type ValidatedDemo = {
  firstName: string;
  lastName: string;
  workEmail: string;
  phone?: string;
  jobTitle: string;
  companyName: string;
  companyWebsite?: string;
  companySize: string;
  industry: string;
  country: string;
  headquartersCity?: string;
  aiSystemsCount: string;
  highRiskExposure: string;
  currentTooling: string;
  primaryInterests: string[];
  timeline: string;
  referralSource: string;
  message?: string;
  marketingConsent: boolean;
};

function validateDemoRequest(
  body: unknown,
): { ok: true; data: ValidatedDemo } | { ok: false; error: string } {
  if (!body || typeof body !== "object") {
    return { ok: false, error: "Invalid request body" };
  }

  const b = body as Record<string, unknown>;

  if (asString(b.website)) {
    return { ok: false, error: "Submission rejected" };
  }

  const formStartedAt = Number(b.formStartedAt);
  if (Number.isFinite(formStartedAt) && Date.now() - formStartedAt < MIN_FILL_MS) {
    return { ok: false, error: "Please take a moment to complete the form" };
  }

  const required = [
    "firstName",
    "lastName",
    "workEmail",
    "jobTitle",
    "companyName",
    "companySize",
    "industry",
    "country",
    "aiSystemsCount",
    "highRiskExposure",
    "currentTooling",
    "timeline",
    "referralSource",
  ] as const;

  for (const field of required) {
    if (!asString(b[field])) {
      return { ok: false, error: `Missing required field: ${field}` };
    }
  }

  const workEmail = asString(b.workEmail);
  if (!EMAIL_RE.test(workEmail)) {
    return { ok: false, error: "Invalid work email address" };
  }

  if (!asBoolean(b.privacyConsent)) {
    return { ok: false, error: "Privacy consent is required" };
  }

  const primaryInterests = Array.isArray(b.primaryInterests)
    ? b.primaryInterests
        .filter((item): item is string => typeof item === "string")
        .map((s) => s.trim())
        .filter(Boolean)
    : [];

  if (primaryInterests.length === 0) {
    return { ok: false, error: "Select at least one area of interest" };
  }

  return {
    ok: true,
    data: {
      firstName: asString(b.firstName),
      lastName: asString(b.lastName),
      workEmail,
      phone: asString(b.phone) || undefined,
      jobTitle: asString(b.jobTitle),
      companyName: asString(b.companyName),
      companyWebsite: asString(b.companyWebsite) || undefined,
      companySize: asString(b.companySize),
      industry: asString(b.industry),
      country: asString(b.country),
      headquartersCity: asString(b.headquartersCity) || undefined,
      aiSystemsCount: asString(b.aiSystemsCount),
      highRiskExposure: asString(b.highRiskExposure),
      currentTooling: asString(b.currentTooling),
      primaryInterests,
      timeline: asString(b.timeline),
      referralSource: asString(b.referralSource),
      message: asString(b.message) || undefined,
      marketingConsent: asBoolean(b.marketingConsent),
    },
  };
}

function buildDiscordEmbed(
  payload: ValidatedDemo,
  meta: { ip?: string; userAgent?: string; submittedAt: string },
) {
  const field = (name: string, value?: string) => ({
    name,
    value: value || "—",
    inline: true,
  });

  return {
    embeds: [
      {
        title: `New demo request — ${payload.companyName}`,
        description: `${payload.firstName} ${payload.lastName} · ${payload.jobTitle}`,
        color: 0x4f46e5,
        fields: [
          field("Work email", payload.workEmail),
          field("Phone", payload.phone),
          field("Company website", payload.companyWebsite),
          field("Company size", payload.companySize),
          field("Industry", payload.industry),
          field("Country", payload.country),
          field("HQ city", payload.headquartersCity),
          field("AI systems", payload.aiSystemsCount),
          field("High-risk exposure", payload.highRiskExposure),
          field("Current tooling", payload.currentTooling),
          field("Primary interests", payload.primaryInterests.join(", ")),
          field("Timeline", payload.timeline),
          field("Referral source", payload.referralSource),
          field("Marketing consent", payload.marketingConsent ? "Yes" : "No"),
          { name: "Message", value: payload.message || "—", inline: false },
        ],
        footer: {
          text: `IP: ${meta.ip || "—"} · ${meta.userAgent || "—"}`.slice(0, 2048),
        },
        timestamp: meta.submittedAt,
      },
    ],
  };
}

function getClientIp(request: NextRequest): string | undefined {
  const forwarded = request.headers.get("x-forwarded-for");
  if (forwarded) {
    return forwarded.split(",")[0]?.trim() || undefined;
  }
  return request.headers.get("x-real-ip") ?? undefined;
}

function isRateLimited(ip: string | undefined): boolean {
  if (!ip) return false;
  const now = Date.now();
  const entry = ipHits.get(ip);
  if (!entry || now > entry.resetAt) {
    ipHits.set(ip, { count: 1, resetAt: now + RATE_LIMIT_WINDOW_MS });
    return false;
  }
  entry.count += 1;
  return entry.count > RATE_LIMIT_MAX;
}

async function sendDemoRequestNotification(
  payload: ValidatedDemo,
  meta: { ip?: string; userAgent?: string; submittedAt: string },
): Promise<void> {
  const webhookUrl =
    process.env.DISCORD_DEMO_WEBHOOK_URL?.trim() ||
    process.env.DISCORD_WEBHOOK_URL?.trim();

  if (!webhookUrl) {
    throw new Error("DISCORD_WEBHOOK_URL is required");
  }

  const response = await fetch(webhookUrl, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(buildDiscordEmbed(payload, meta)),
  });

  if (!response.ok) {
    throw new Error(`Discord webhook responded with ${response.status}`);
  }
}

export async function POST(request: NextRequest) {
  const ip = getClientIp(request);
  if (isRateLimited(ip)) {
    return NextResponse.json(
      { error: "Too many requests. Please try again later." },
      { status: 429 },
    );
  }

  let body: unknown;
  try {
    body = await request.json();
  } catch {
    return NextResponse.json({ error: "Invalid request body" }, { status: 400 });
  }

  const parsed = validateDemoRequest(body);
  if (!parsed.ok) {
    return NextResponse.json({ error: parsed.error }, { status: 400 });
  }

  try {
    await sendDemoRequestNotification(parsed.data, {
      submittedAt: new Date().toISOString(),
      userAgent: request.headers.get("user-agent") ?? undefined,
      ip,
    });
    return NextResponse.json({ ok: true });
  } catch (error) {
    console.error("Demo request notification failed:", error);
    return NextResponse.json(
      {
        error: `Unable to send your request right now. Please email ${siteConfig.supportEmail} directly.`,
      },
      { status: 503 },
    );
  }
}

export async function OPTIONS() {
  return new NextResponse(null, {
    status: 204,
    headers: {
      Allow: "POST, OPTIONS",
      "Access-Control-Allow-Methods": "POST, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type",
    },
  });
}
