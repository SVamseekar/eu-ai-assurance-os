import type { NextRequest, NextResponse } from "next/server";

const ACCESS_COOKIE = "session_access";
const REFRESH_COOKIE = "session_refresh";

function cookieBase() {
  return {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: "lax" as const,
    path: "/",
  };
}

export function setSessionCookies(
  response: NextResponse,
  accessToken: string,
  refreshToken: string,
) {
  const base = cookieBase();
  response.cookies.set(ACCESS_COOKIE, accessToken, {
    ...base,
    maxAge: 15 * 60,
  });
  response.cookies.set(REFRESH_COOKIE, refreshToken, {
    ...base,
    maxAge: 30 * 24 * 60 * 60,
  });
}

export function clearSessionCookies(response: NextResponse) {
  const base = cookieBase();
  // Match set() attributes so production Secure cookies actually expire.
  response.cookies.set(ACCESS_COOKIE, "", { ...base, maxAge: 0 });
  response.cookies.set(REFRESH_COOKIE, "", { ...base, maxAge: 0 });
}

export function readAccessToken(request: NextRequest): string | undefined {
  return request.cookies.get(ACCESS_COOKIE)?.value;
}

export function readRefreshToken(request: NextRequest): string | undefined {
  return request.cookies.get(REFRESH_COOKIE)?.value;
}

export const ACCESS_COOKIE_NAME = ACCESS_COOKIE;
export const REFRESH_COOKIE_NAME = REFRESH_COOKIE;

const API_BASE = process.env.ASSURANCE_API_BASE_URL ?? "http://localhost:8080";

export async function refreshAccessToken(
  refreshToken: string,
): Promise<{ accessToken: string; refreshToken: string } | null> {
  const upstream = await fetch(`${API_BASE}/auth/refresh`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken }),
  });
  if (!upstream.ok) {
    return null;
  }
  const tokens = await upstream.json();
  return { accessToken: tokens.accessToken, refreshToken: tokens.refreshToken };
}