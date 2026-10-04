import { NextResponse } from "next/server";
import { setSessionCookies } from "@/lib/session";
import { fetchUpstream, serviceUnavailable } from "@/lib/upstream";

const API_BASE = process.env.ASSURANCE_API_BASE_URL ?? "http://localhost:8080";

/** Starts the read-only demo session: an access cookie only, no refresh token. */
export async function POST() {
  const upstream = await fetchUpstream(`${API_BASE}/auth/demo`, { method: "POST" });
  if (!upstream || upstream.status >= 500) return serviceUnavailable();
  if (!upstream.ok) {
    return NextResponse.json({ error: "The demo is not available right now." }, { status: 404 });
  }
  const tokens = await upstream.json();
  const response = NextResponse.json({ ok: true, next: "/command" });
  setSessionCookies(response, tokens.accessToken, "");
  return response;
}
