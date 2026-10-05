import { readFileSync } from "node:fs";
import { join } from "node:path";

export interface ChangelogEntry {
  date: string;
  title: string;
  items: string[];
}

/** Parses content/changelog.md: "## YYYY-MM-DD — Title" headings followed by "- " bullets. */
export function parseChangelog(markdown: string): ChangelogEntry[] {
  const entries: ChangelogEntry[] = [];
  for (const line of markdown.split("\n")) {
    const heading = line.match(/^## (\d{4}-\d{2}-\d{2}) — (.+)$/);
    if (heading) {
      entries.push({ date: heading[1], title: heading[2].trim(), items: [] });
      continue;
    }
    const item = line.match(/^- (.+)$/);
    if (item && entries.length) entries[entries.length - 1].items.push(item[1].trim());
  }
  return entries.sort((a, b) => b.date.localeCompare(a.date));
}

export function loadChangelog(): ChangelogEntry[] {
  return parseChangelog(readFileSync(join(process.cwd(), "content", "changelog.md"), "utf8"));
}
