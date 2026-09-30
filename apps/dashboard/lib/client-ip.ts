import type { NextRequest } from "next/server";

/** Real browser IP for the API's per-IP auth rate limit (trusted only when the API sits behind this BFF). */
export function clientIpHeaders(request: NextRequest): Record<string, string> {
  const ip =
    request.headers.get("cf-connecting-ip") ??
    request.headers.get("x-forwarded-for")?.split(",")[0]?.trim() ??
    "";
  return ip ? { "X-Client-IP": ip } : {};
}
