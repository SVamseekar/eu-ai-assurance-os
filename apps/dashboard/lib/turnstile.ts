const VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

export async function verifyTurnstile(
  token: string | null,
  ip: string | null,
  secret: string = process.env.TURNSTILE_SECRET_KEY ?? "",
): Promise<boolean> {
  if (!secret) {
    // Missing secret must not silently disable the challenge in production.
    // Local runs and the e2e harness opt out explicitly with TURNSTILE_DISABLED=1.
    if (process.env.NODE_ENV !== "production" || process.env.TURNSTILE_DISABLED === "1") return true;
    console.error("TURNSTILE_SECRET_KEY is not set; refusing unverified request");
    return false;
  }
  if (!token) return false;
  try {
    const body = new URLSearchParams({ secret, response: token });
    if (ip) body.set("remoteip", ip);
    const res = await fetch(VERIFY_URL, { method: "POST", body });
    const data = (await res.json()) as { success?: boolean };
    return data.success === true;
  } catch {
    return false;
  }
}
