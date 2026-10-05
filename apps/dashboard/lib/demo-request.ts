/** Short demo form (storyboard frame 12): the four fields a first conversation needs. */
export type DemoRequestInput = {
  workEmail: string;
  companyName: string;
  /** Role picked from DEMO_ROLES. */
  jobTitle: string;
  message: string;
  marketingConsent: boolean;
  privacyConsent: boolean;
  /** Honeypot — must stay empty */
  website: string;
  formStartedAt: number;
};

export const DEMO_ROLES = [
  "Engineering",
  "Data / ML",
  "Product",
  "Compliance / risk",
  "Legal",
  "Audit",
  "Leadership",
  "Other",
] as const;

export async function submitDemoRequest(payload: DemoRequestInput): Promise<void> {
  const response = await fetch("/api/request-demo", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });

  const data = (await response.json().catch(() => ({}))) as { error?: string };

  if (!response.ok) {
    throw new Error(data.error ?? "Failed to submit demo request");
  }
}
