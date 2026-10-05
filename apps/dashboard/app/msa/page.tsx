import type { Metadata } from "next";
import Link from "next/link";

import { LegalPage, LegalSection } from "@/components/landing/legal-page-shell";
import { COMMON_PAPER } from "@/lib/legal";
import { siteConfig } from "@/lib/site-config";

const title = "Master agreement";
const description = `How ${siteConfig.shortName} Enterprise agreements are put together: the Common Paper Cloud Service Agreement, a signed order form, and the DPA.`;
const path = "/msa";

export const metadata: Metadata = { title, description, alternates: { canonical: path } };

export default function MsaPage() {
  return (
    <LegalPage title={title} description={description} path={path}>
      <LegalSection title="The agreement">
        <p>
          Enterprise subscriptions use the{" "}
          <a href={COMMON_PAPER.cloudService.url} target="_blank" rel="noopener noreferrer">
            {COMMON_PAPER.cloudService.name}, version {COMMON_PAPER.cloudService.version}
          </a>{" "}
          ({COMMON_PAPER.license.name}) as the master agreement. We do not use a separate bespoke MSA.
        </p>
      </LegalSection>
      <LegalSection title="What makes up a signed agreement">
        <ul>
          <li>
            The signed <Link href="/order-form">order form</Link> (the Cover Page: plan, term, systems, fees and key
            terms).
          </li>
          <li>The Common Paper Cloud Service Agreement Standard Terms, version {COMMON_PAPER.cloudService.version}.</li>
          <li>
            The <Link href="/dpa">Data Processing Agreement</Link> and the <Link href="/security">security page</Link> as
            the Security Policy.
          </li>
        </ul>
        <p>If documents conflict, the order form wins, then the DPA, then the Standard Terms.</p>
      </LegalSection>
      <LegalSection title="What we supply">
        <p>
          A hosted software subscription: release gates, evidence records and signed evidence packs, with onboarding
          assistance. Outputs are evidence and readiness aids. {siteConfig.shortName} is not legal advice, not a notified
          body, and does not certify compliance; the customer decides whether a system ships.
        </p>
      </LegalSection>
      <LegalSection title="Self-serve plans">
        <p>
          Free, Team and Business plans are covered by the <Link href="/terms">Terms of Service</Link>, which use the
          same Standard Terms by reference.
        </p>
      </LegalSection>
    </LegalPage>
  );
}
