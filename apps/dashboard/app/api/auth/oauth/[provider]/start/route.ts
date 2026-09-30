import { randomBytes } from "node:crypto";
import { NextRequest, NextResponse } from "next/server";
import { safeNextPath } from "@/lib/auth-redirect";

const API_BASE = process.env.ASSURANCE_API_BASE_URL ?? "http://localhost:8080";
const SUPPORTED = new Set(["google", "microsoft"]);
const OAUTH_NEXT_COOKIE = "oauth_next";
const OAUTH_NONCE_COOKIE = "oauth_nonce";

/**
 * Browser entry for OAuth: stores safe return path, then redirects to the Spring
 * start endpoint (302 to Google/Microsoft with a signed state parameter).
 */
export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ provider: string }> },
) {
  const { provider } = await params;
  const normalized = provider?.toLowerCase();
  if (!SUPPORTED.has(normalized)) {
    return NextResponse.redirect(new URL("/login?auth_error=unsupported_provider", request.url));
  }

  const next = safeNextPath(request.nextUrl.searchParams.get("next"));
  const nonce = randomBytes(24).toString("base64url");
  const response = NextResponse.redirect(
    `${API_BASE}/auth/oauth/${normalized}/start?nonce=${encodeURIComponent(nonce)}`,
    302,
  );
  const cookieOptions = {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: "lax" as const,
    path: "/",
    maxAge: 10 * 60,
  };
  response.cookies.set(OAUTH_NEXT_COOKIE, next, cookieOptions);
  response.cookies.set(OAUTH_NONCE_COOKIE, nonce, cookieOptions);
  return response;
}
