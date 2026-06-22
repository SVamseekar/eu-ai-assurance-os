import { NextRequest, NextResponse } from "next/server";
import { setSessionCookies } from "@/lib/session";

const API_BASE = process.env.ASSURANCE_API_BASE_URL ?? "http://localhost:8080";

export async function POST(request: NextRequest) {
  const { email, password } = await request.json();

  const upstream = await fetch(`${API_BASE}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });

  if (!upstream.ok) {
    return NextResponse.json({ error: "Invalid email or password" }, { status: 401 });
  }

  const tokens = await upstream.json();
  const response = NextResponse.json({ ok: true });
  setSessionCookies(response, tokens.accessToken, tokens.refreshToken);
  return response;
}