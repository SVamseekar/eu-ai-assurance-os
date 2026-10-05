#!/usr/bin/env node
// Exports the pinned EU AI Act provisions that back the public /eu-ai-act/<slug> pages.
//
//   node scripts/export-corpus-pages.mjs http://localhost:8080 <api-key>
//
// Reads GET /api/v1/corpus and writes apps/dashboard/content/corpus/provisions.json.
// Titles come from the official article and annex headings, which the corpus API does not return.
import { mkdir, writeFile } from "node:fs/promises";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const [baseUrl = "http://localhost:8080", apiKey] = process.argv.slice(2);
if (!apiKey) {
  console.error("usage: node scripts/export-corpus-pages.mjs <api-base-url> <api-key>");
  process.exit(2);
}

const SEED_CELEX = "32024R1689";
// ELI of the consolidated version the excerpts come from.
const ELI = "https://eur-lex.europa.eu/eli/reg/2024/1689/2026-07-27";

const TITLES = {
  4: "AI literacy",
  5: "Prohibited AI practices",
  6: "Classification rules for high-risk AI systems",
  9: "Risk management system",
  10: "Data and data governance",
  11: "Technical documentation",
  12: "Record-keeping",
  13: "Transparency and provision of information to deployers",
  14: "Human oversight",
  15: "Accuracy, robustness and cybersecurity",
  26: "Obligations of deployers of high-risk AI systems",
  27: "Fundamental rights impact assessment for high-risk AI systems",
  50: "Transparency obligations for providers and deployers of certain AI systems",
  72: "Post-market monitoring by providers and post-market monitoring plan for high-risk AI systems",
  III: "High-risk AI systems referred to in Article 6(2)",
  IV: "Technical documentation referred to in Article 11(1)",
};

const res = await fetch(`${baseUrl.replace(/\/$/, "")}/api/v1/corpus`, { headers: { "X-Api-Key": apiKey } });
if (!res.ok) {
  console.error(`GET /api/v1/corpus failed: ${res.status}`);
  process.exit(1);
}
const corpus = await res.json();
const instrument = corpus.instruments.find((i) => i.seedCelex === SEED_CELEX);
if (!instrument) {
  console.error(`${SEED_CELEX} is not in the corpus`);
  process.exit(1);
}
const prefix = `${instrument.consolidationCelex ?? SEED_CELEX}#`;

const records = [];
for (const [ref, title] of Object.entries(TITLES)) {
  const isAnnex = Number.isNaN(Number(ref));
  // Paragraph 1 of each article (or the article itself when it has no paragraphs); the whole annex.
  const candidates = corpus.provisions.filter(
    (p) => p.provisionKey.startsWith(prefix) && (isAnnex ? p.annex === `annex${ref}` : p.article === ref && !p.annex),
  );
  const provision = candidates.find((p) => isAnnex || p.paragraph === "1" || !p.paragraph) ?? candidates[0];
  if (!provision) {
    console.error(`missing ${isAnnex ? "Annex" : "Article"} ${ref} in the pinned corpus`);
    process.exit(1);
  }
  records.push({
    slug: isAnnex ? `annex-${ref.toLowerCase()}` : `article-${ref}`,
    label: isAnnex ? `Annex ${ref}` : `Article ${ref}`,
    provisionKey: provision.provisionKey,
    title,
    excerpt: provision.textExcerpt,
    forceStatus: provision.forceStatus,
    forceFrom: provision.forceFrom,
    scopeNote: provision.scopeNote ?? null,
    sourceUrl: isAnnex ? `${ELI}#anx_${ref}` : `${ELI}#art_${ref}`,
  });
}

const out = join(dirname(fileURLToPath(import.meta.url)), "..", "apps", "dashboard", "content", "corpus", "provisions.json");
await mkdir(dirname(out), { recursive: true });
await writeFile(
  out,
  `${JSON.stringify({ corpusVersion: corpus.corpusVersion, consolidationCelex: instrument.consolidationCelex, attribution: corpus.attribution, provisions: records }, null, 2)}\n`,
);
console.log(`wrote ${records.length} provisions to ${out}`);
