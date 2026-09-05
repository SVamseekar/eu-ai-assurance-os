/** Safe same-origin path for post-login redirects (`?next=` / OAuth return). */
export function safeNextPath(value: string | null | undefined, fallback = "/command"): string {
  if (!value) return fallback;
  if (!value.startsWith("/") || value.startsWith("//")) return fallback;
  if (value.startsWith("/api/") || value.startsWith("/login")) return fallback;
  return value;
}
