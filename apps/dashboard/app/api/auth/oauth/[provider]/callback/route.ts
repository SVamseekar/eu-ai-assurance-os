import { NextRequest, NextResponse } from "next/server";
import { setSessionCookies } from "@/lib/session";

const API_BASE = process.env.ASSURANCE_API_BASE_URL ?? "http://localhost:8080";
const SUPPORTED = new Set(["google", "microsoft"]);

function loginRedirect(request: NextRequest, authError: string): NextResponse {
  const url = new URL("/login", request.url);
  url.searchParams.set("auth_error", authError);
  return NextResponse.redirect(url);
}

/**
 * OIDC redirect_uri target. Exchanges code+state with the Spring API, then sets
 * the same httpOnly session cookies as password login.
 */
export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ provider: string }> },
) {
  const { provider } = await params;
  const normalized = provider?.toLowerCase();
  if (!SUPPORTED.has(normalized)) {
    return loginRedirect(request, "unsupported_provider");
  }

  const oauthError = request.nextUrl.searchParams.get("error");
  if (oauthError) {
    const code = oauthError === "access_denied" ? "denied" : "sign_in_failed";
    return loginRedirect(request, code);
  }

  const code = request.nextUrl.searchParams.get("code");
  const state = request.nextUrl.searchParams.get("state");
  if (!code || !state) {
    return loginRedirect(request, "state");
  }

  let upstream: Response;
  try {
    upstream = await fetch(`${API_BASE}/auth/oauth/${normalized}/callback`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ code, state }),
      cache: "no-store",
    });
  } catch {
    return loginRedirect(request, "sign_in_unavailable");
  }

  if (!upstream.ok) {
    if (upstream.status === 403) {
      return loginRedirect(request, "not_provisioned");
    }
    if (upstream.status === 400) {
      return loginRedirect(request, "state");
    }
    return loginRedirect(request, "sign_in_failed");
  }

  const tokens = (await upstream.json()) as {
    accessToken?: string;
    refreshToken?: string;
  };
  if (!tokens.accessToken || !tokens.refreshToken) {
    return loginRedirect(request, "sign_in_failed");
  }

  const destination = new URL("/", request.url);
  const response = NextResponse.redirect(destination);
  setSessionCookies(response, tokens.accessToken, tokens.refreshToken);
  return response;
}
