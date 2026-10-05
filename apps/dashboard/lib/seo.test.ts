import assert from "node:assert/strict";
import { test } from "node:test";

import robots from "../app/robots";
import sitemap from "../app/sitemap";
import { siteConfig } from "./site-config";

const paths = sitemap().map((entry) => entry.url.replace(siteConfig.url, "") || "/");

test("sitemap lists public pages, tools, comparison, blog and provision pages", () => {
  for (const path of [
    "/pricing",
    "/security",
    "/tools/ai-act-check",
    "/eu-ai-act/article-50",
    "/compare/credo-ai-alternative",
    "/blog/fail-closed-release-gate-github-actions",
    "/docs",
    "/changelog",
  ]) {
    assert.ok(paths.includes(path), `sitemap has ${path}`);
  }
});

test("sitemap leaves out auth and app pages", () => {
  for (const path of ["/login", "/signup", "/onboarding", "/command", "/settings", "/invite"]) {
    assert.ok(!paths.includes(path), `sitemap omits ${path}`);
  }
});

test("robots disallows the app routes and the API", () => {
  const rules = robots().rules;
  const rule = Array.isArray(rules) ? rules[0] : rules;
  const disallow = ([] as string[]).concat(rule.disallow ?? []);
  for (const path of ["/command", "/systems", "/settings", "/corpus", "/proposals", "/onboarding", "/api/"]) {
    assert.ok(disallow.includes(path), `robots disallows ${path}`);
  }
});
