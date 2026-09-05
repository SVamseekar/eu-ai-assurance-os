import { NextRequest, NextResponse } from "next/server";
import { safeNextPath } from "@/lib/auth-redirect";

const ACCESS_COOKIE = "session_access";
const REFRESH_COOKIE = "session_refresh";

/** Dashboard app shell routes that require a session cookie (defense in depth). */
const PROTECTED_PREFIXES = [
  "/command",
  "/systems",
  "/approvals",
  "/evidence",
  "/evals",
  "/contracts",
  "/audit",
  "/readiness",
  "/reg-monitor",
];

function isProtectedPath(pathname: string): boolean {
  return PROTECTED_PREFIXES.some(
    (prefix) => pathname === prefix || pathname.startsWith(`${prefix}/`),
  );
}

function hasSession(request: NextRequest): boolean {
  return (
    Boolean(request.cookies.get(ACCESS_COOKIE)?.value) ||
    Boolean(request.cookies.get(REFRESH_COOKIE)?.value)
  );
}

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  if (pathname === "/login" || pathname.startsWith("/login/")) {
    if (hasSession(request)) {
      const next = safeNextPath(request.nextUrl.searchParams.get("next"));
      return NextResponse.redirect(new URL(next, request.url));
    }
    return NextResponse.next();
  }

  if (!isProtectedPath(pathname)) {
    return NextResponse.next();
  }

  if (hasSession(request)) {
    return NextResponse.next();
  }

  const loginUrl = request.nextUrl.clone();
  loginUrl.pathname = "/login";
  loginUrl.searchParams.set("next", pathname);
  return NextResponse.redirect(loginUrl);
}

export const config = {
  matcher: [
    "/login",
    "/command",
    "/command/:path*",
    "/systems",
    "/systems/:path*",
    "/approvals",
    "/approvals/:path*",
    "/evidence",
    "/evidence/:path*",
    "/evals",
    "/evals/:path*",
    "/contracts",
    "/contracts/:path*",
    "/audit",
    "/audit/:path*",
    "/readiness",
    "/readiness/:path*",
    "/reg-monitor",
    "/reg-monitor/:path*",
  ],
};
