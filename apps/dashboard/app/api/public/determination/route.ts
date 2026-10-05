import { NextRequest, NextResponse } from "next/server";

import { clientIpHeaders } from "@/lib/client-ip";
import { readLimitedText } from "@/lib/read-limited-body";
import { fetchUpstream, serviceUnavailable } from "@/lib/upstream";

const API_BASE = process.env.ASSURANCE_API_BASE_URL ?? "http://localhost:8080";
const MAX_BODY_BYTES = 16_384;

/** Questionnaire for the free AI Act check. No session needed. */
export async function GET() {
  const upstream = await fetchUpstream(`${API_BASE}/api/public/v1/determination/questionnaire`);
  if (!upstream || !upstream.ok) return serviceUnavailable();
  return NextResponse.json(await upstream.json(), { headers: { "Cache-Control": "public, max-age=3600" } });
}

/** Preview of likely obligations. Nothing is stored; the API rate-limits per visitor IP. */
export async function POST(request: NextRequest) {
  // Bounded read: an unauthenticated route must not buffer an arbitrarily large body.
  const body = await readLimitedText(request, MAX_BODY_BYTES);
  if (body === null) {
    return NextResponse.json({ error: "Request body too large" }, { status: 400 });
  }
  const upstream = await fetchUpstream(`${API_BASE}/api/public/v1/determination/preview`, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...clientIpHeaders(request) },
    body,
  });
  if (!upstream || upstream.status >= 500) return serviceUnavailable();
  if (upstream.status === 429) {
    return NextResponse.json({ error: "Too many checks from your network. Try again in 15 minutes." }, { status: 429 });
  }
  return NextResponse.json(await upstream.json().catch(() => ({})), { status: upstream.status });
}
