import { NextRequest, NextResponse } from "next/server";

const API_BASE = process.env.ASSURANCE_API_BASE_URL ?? "http://localhost:8080";
const SUPPORTED = new Set(["google", "microsoft"]);

/**
 * Browser entry for OAuth: redirects to the Spring start endpoint, which
 * 302s to Google/Microsoft with a signed state parameter.
 */
export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ provider: string }> },
) {
  const { provider } = await params;
  const normalized = provider?.toLowerCase();
  if (!SUPPORTED.has(normalized)) {
    return NextResponse.redirect(new URL("/login?auth_error=unsupported_provider", _request.url));
  }

  // 302 through the API so authorization URL + signed state are server-built.
  return NextResponse.redirect(`${API_BASE}/auth/oauth/${normalized}/start`, 302);
}
