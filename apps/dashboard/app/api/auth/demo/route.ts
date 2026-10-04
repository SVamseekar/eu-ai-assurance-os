import { NextRequest, NextResponse } from "next/server";
import { clientIpHeaders } from "@/lib/client-ip";
import { REFRESH_COOKIE_NAME, setSessionCookies } from "@/lib/session";
import { fetchUpstream, serviceUnavailable } from "@/lib/upstream";

const API_BASE = process.env.ASSURANCE_API_BASE_URL ?? "http://localhost:8080";

/** Starts the read-only demo session: an access cookie only, no refresh token. */
export async function POST(request: NextRequest) {
  // Forward the visitor's IP: without it every visitor shares the BFF's rate-limit bucket.
  const upstream = await fetchUpstream(`${API_BASE}/auth/demo`, {
    method: "POST",
    headers: clientIpHeaders(request),
  });
  if (!upstream || upstream.status >= 500) return serviceUnavailable();
  if (!upstream.ok) {
    return NextResponse.json({ error: "The demo is not available right now." }, { status: 404 });
  }
  const tokens = await upstream.json();
  const response = NextResponse.json({ ok: true, next: "/command" });
  setSessionCookies(response, tokens.accessToken, "");
  // A refresh cookie from an earlier real session would let the proxy swap this demo for that account.
  response.cookies.set(REFRESH_COOKIE_NAME, "", { path: "/", maxAge: 0 });
  return response;
}
