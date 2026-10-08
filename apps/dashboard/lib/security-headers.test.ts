import assert from "node:assert/strict";
import { describe, it } from "node:test";
import { securityHeaders } from "./security-headers";

describe("securityHeaders", () => {
  const byKey = Object.fromEntries(securityHeaders(false).map((h) => [h.key, h.value]));
  it("forbids framing and sniffing", () => {
    assert.match(byKey["Content-Security-Policy"], /frame-ancestors 'none'/);
    assert.equal(byKey["X-Content-Type-Options"], "nosniff");
    assert.equal(byKey["Referrer-Policy"], "strict-origin-when-cross-origin");
  });
  it("allows only Cloudflare (analytics beacon, Turnstile) and consent-gated gtag.js as third-party script", () => {
    assert.match(
      byKey["Content-Security-Policy"],
      /script-src 'self' 'unsafe-inline' https:\/\/static\.cloudflareinsights\.com https:\/\/challenges\.cloudflare\.com https:\/\/www\.googletagmanager\.com;/,
    );
  });
  it("lets GA4 send hits only to Google Analytics endpoints", () => {
    assert.match(byKey["Content-Security-Policy"], /connect-src [^;]*https:\/\/\*\.google-analytics\.com/);
    assert.doesNotMatch(byKey["Content-Security-Policy"], /doubleclick/);
  });
  it("lets the Turnstile challenge frame itself and nothing else", () => {
    assert.match(byKey["Content-Security-Policy"], /frame-src https:\/\/challenges\.cloudflare\.com(;|$)/);
  });
  it("allows eval only in development", () => {
    assert.doesNotMatch(byKey["Content-Security-Policy"], /unsafe-eval/);
    const dev = Object.fromEntries(securityHeaders(true).map((h) => [h.key, h.value]));
    assert.match(dev["Content-Security-Policy"], /script-src 'self' 'unsafe-inline' 'unsafe-eval'/);
  });
});
