/** Regulatory dates shown on the public site. One edit here updates the deadline bar and the EU AI Act page. */
export interface Deadline {
  id: string;
  region: "EU" | "US" | "AU" | "NZ";
  label: string;
  /** ISO date the duty applies from. */
  date: string;
  source: { title: string; url: string };
}

const AI_ACT = { title: "Regulation (EU) 2024/1689 (EUR-Lex)", url: "https://eur-lex.europa.eu/eli/reg/2024/1689/oj" };
const OMNIBUS = { title: "Regulation (EU) 2026/1744 (EUR-Lex)", url: "https://eur-lex.europa.eu/eli/reg/2026/1744/oj" };

export const DEADLINES: Deadline[] = [
  { id: "eu-art50", region: "EU", label: "Article 50 transparency duties", date: "2026-08-02", source: AI_ACT },
  { id: "eu-art50-2", region: "EU", label: "Article 50(2) marking of AI-generated content", date: "2026-12-02", source: OMNIBUS },
  { id: "eu-annex-iii", region: "EU", label: "Annex III high-risk duties", date: "2027-12-02", source: OMNIBUS },
  { id: "eu-annex-i", region: "EU", label: "Annex I embedded high-risk duties", date: "2028-08-02", source: OMNIBUS },
];

export function deadlineStatus(d: Deadline, today: Date): "applies-now" | "upcoming" {
  return new Date(`${d.date}T00:00:00Z`).getTime() <= today.getTime() ? "applies-now" : "upcoming";
}

/** The soonest deadline that has not started yet, or null when every date has passed. */
export function nextDeadline(today: Date, list: Deadline[] = DEADLINES): Deadline | null {
  return (
    list
      .filter((d) => deadlineStatus(d, today) === "upcoming")
      .sort((a, b) => a.date.localeCompare(b.date))[0] ?? null
  );
}

export function formatDeadlineDate(iso: string): string {
  return new Date(`${iso}T00:00:00Z`).toLocaleDateString("en-GB", {
    day: "numeric",
    month: "short",
    year: "numeric",
    timeZone: "UTC",
  });
}
