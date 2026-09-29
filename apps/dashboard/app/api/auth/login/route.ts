import { NextRequest, NextResponse } from "next/server";
import { setSessionCookies } from "@/lib/session";

const API_BASE = process.env.ASSURANCE_API_BASE_URL ?? "http://localhost:8080";

export async function POST(request: NextRequest) {
  const { email, password } = await request.json();

  const clientIp =
    request.headers.get("cf-connecting-ip") ??
    request.headers.get("x-forwarded-for")?.split(",")[0]?.trim() ??
    "";
  const upstream = await fetch(`${API_BASE}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...(clientIp ? { "X-Client-IP": clientIp } : {}) },
    body: JSON.stringify({ email, password }),
  });

  if (upstream.status === 429) {
    return NextResponse.json({ error: "Too many sign-in attempts. Try again in 15 minutes." }, { status: 429 });
  }

  if (!upstream.ok) {
    return NextResponse.json({ error: "Invalid email or password" }, { status: 401 });
  }

  const tokens = await upstream.json();
  const response = NextResponse.json({ ok: true });
  setSessionCookies(response, tokens.accessToken, tokens.refreshToken);
  return response;
}