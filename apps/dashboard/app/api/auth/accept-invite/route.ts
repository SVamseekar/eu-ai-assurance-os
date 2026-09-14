import { NextRequest, NextResponse } from "next/server";
import { setSessionCookies } from "@/lib/session";

const API_BASE = process.env.ASSURANCE_API_BASE_URL ?? "http://localhost:8080";

export async function POST(request: NextRequest) {
  const body = await request.json();
  const upstream = await fetch(`${API_BASE}/auth/accept-invite`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  if (!upstream.ok) {
    const text = await upstream.text();
    return NextResponse.json(
      { error: text || "Invite could not be accepted" },
      { status: upstream.status },
    );
  }
  const tokens = await upstream.json();
  const response = NextResponse.json({ ok: true });
  setSessionCookies(response, tokens.accessToken, tokens.refreshToken);
  return response;
}
