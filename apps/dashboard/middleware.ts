import { NextRequest, NextResponse } from "next/server";

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
];

function isProtectedPath(pathname: string): boolean {
  return PROTECTED_PREFIXES.some(
    (prefix) => pathname === prefix || pathname.startsWith(`${prefix}/`),
  );
}

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  if (!isProtectedPath(pathname)) {
    return NextResponse.next();
  }

  const hasSession =
    Boolean(request.cookies.get(ACCESS_COOKIE)?.value) ||
    Boolean(request.cookies.get(REFRESH_COOKIE)?.value);

  if (hasSession) {
    return NextResponse.next();
  }

  const loginUrl = request.nextUrl.clone();
  loginUrl.pathname = "/login";
  loginUrl.searchParams.set("next", pathname);
  return NextResponse.redirect(loginUrl);
}

export const config = {
  matcher: [
    "/command/:path*",
    "/systems/:path*",
    "/approvals/:path*",
    "/evidence/:path*",
    "/evals/:path*",
    "/contracts/:path*",
    "/audit/:path*",
  ],
};
