import assert from "node:assert/strict";
import { describe, it } from "node:test";
import { securityHeaders } from "./security-headers";

describe("securityHeaders", () => {
  const byKey = Object.fromEntries(securityHeaders().map((h) => [h.key, h.value]));
  it("forbids framing and sniffing", () => {
    assert.match(byKey["Content-Security-Policy"], /frame-ancestors 'none'/);
    assert.equal(byKey["X-Content-Type-Options"], "nosniff");
    assert.equal(byKey["Referrer-Policy"], "strict-origin-when-cross-origin");
  });
  it("allows only Cloudflare (analytics beacon, Turnstile) as third-party script", () => {
    assert.match(
      byKey["Content-Security-Policy"],
      /script-src 'self' 'unsafe-inline' https:\/\/static\.cloudflareinsights\.com https:\/\/challenges\.cloudflare\.com/,
    );
    assert.doesNotMatch(byKey["Content-Security-Policy"], /googletagmanager/);
  });
  it("lets the Turnstile challenge frame itself and nothing else", () => {
    assert.match(byKey["Content-Security-Policy"], /frame-src https:\/\/challenges\.cloudflare\.com(;|$)/);
  });
});
