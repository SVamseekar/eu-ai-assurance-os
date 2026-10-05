import { siteConfig } from "@/lib/site-config";

/** RFC 9116 security.txt. Expires one year after the response is generated. */
export function GET() {
  const expires = new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString().replace(/\.\d{3}Z$/, "Z");
  const body = [
    `Contact: mailto:${siteConfig.securityEmail}`,
    `Expires: ${expires}`,
    "Preferred-Languages: en",
    `Policy: ${siteConfig.url}/security`,
    `Canonical: ${siteConfig.url}/.well-known/security.txt`,
    "",
  ].join("\n");
  return new Response(body, { headers: { "Content-Type": "text/plain; charset=utf-8" } });
}
