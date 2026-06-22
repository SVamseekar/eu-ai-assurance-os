import type { NextRequest, NextResponse } from "next/server";

const ACCESS_COOKIE = "session_access";
const REFRESH_COOKIE = "session_refresh";

export function setSessionCookies(
  response: NextResponse,
  accessToken: string,
  refreshToken: string,
) {
  const isProduction = process.env.NODE_ENV === "production";
  response.cookies.set(ACCESS_COOKIE, accessToken, {
    httpOnly: true,
    secure: isProduction,
    sameSite: "lax",
    path: "/",
    maxAge: 15 * 60,
  });
  response.cookies.set(REFRESH_COOKIE, refreshToken, {
    httpOnly: true,
    secure: isProduction,
    sameSite: "lax",
    path: "/",
    maxAge: 30 * 24 * 60 * 60,
  });
}

export function clearSessionCookies(response: NextResponse) {
  response.cookies.delete(ACCESS_COOKIE);
  response.cookies.delete(REFRESH_COOKIE);
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