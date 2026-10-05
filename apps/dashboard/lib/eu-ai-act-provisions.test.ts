import assert from "node:assert/strict";
import { test } from "node:test";

import { CORPUS_ATTRIBUTION, PROVISIONS, PROVISION_GUIDES } from "./eu-ai-act-provisions";

// Codes from ControlService.baselineControls() in the API.
const CATALOG = new Set([
  "RISK_MANAGEMENT",
  "DATA_GOVERNANCE",
  "RECORD_KEEPING",
  "TRANSPARENCY",
  "HUMAN_OVERSIGHT",
  "ACCURACY_ROBUSTNESS",
  "CYBERSECURITY",
  "TECHNICAL_DOCUMENTATION",
]);

test("exports the pinned articles and annexes", () => {
  const slugs = PROVISIONS.map((p) => p.slug);
  for (const n of [4, 5, 6, 9, 10, 11, 12, 13, 14, 15, 26, 27, 50, 72]) assert.ok(slugs.includes(`article-${n}`), `article-${n}`);
  assert.ok(slugs.includes("annex-iii"));
  assert.ok(slugs.includes("annex-iv"));
});

test("every provision has an excerpt, a force date, an EUR-Lex source and a guide", () => {
  assert.match(CORPUS_ATTRIBUTION, /EUR-Lex/);
  for (const p of PROVISIONS) {
    assert.ok(p.excerpt.length > 20, p.slug);
    assert.match(p.forceFrom, /^\d{4}-\d{2}-\d{2}$/);
    assert.match(p.sourceUrl, /^https:\/\/eur-lex\.europa\.eu\//);
    const guide = PROVISION_GUIDES[p.slug];
    assert.ok(guide, `guide for ${p.slug}`);
    assert.ok(guide.evidence.length > 0);
    for (const code of guide.controls) assert.ok(CATALOG.has(code), `${p.slug}: ${code}`);
  }
});

test("force dates follow Article 113 as amended", () => {
  const date = (slug: string) => PROVISIONS.find((p) => p.slug === slug)?.forceFrom;
  assert.equal(date("article-4"), "2025-02-02");
  assert.equal(date("article-14"), "2027-12-02");
  assert.equal(date("article-50"), "2026-08-02");
  assert.equal(date("annex-iii"), "2027-12-02");
});
