import type { Metadata } from "next";
import Link from "next/link";

import { LegalPage, LegalSection } from "@/components/landing/legal-page-shell";
import { siteConfig } from "@/lib/site-config";

const title = "Security";
const description = `The security controls ${siteConfig.shortName} runs today, self-attested, and how to report a vulnerability.`;
const path = "/security";

export const metadata: Metadata = { title, description, alternates: { canonical: path } };

const docs = `${siteConfig.githubUrl}/blob/main/docs`;

const controls: [string, React.ReactNode][] = [
  ["EU hosting", "Production runs on Oracle Cloud in the EU (Frankfurt/Amsterdam)."],
  ["TLS everywhere", "All traffic is encrypted in transit through Cloudflare; the API is not exposed to the internet."],
  [
    "Encryption at rest",
    "Block volumes are encrypted by default. Backups are encrypted with age before upload to an EU-jurisdiction bucket.",
  ],
  ["Tenant isolation", "Every query is scoped to the caller's workspace, and isolation is covered by automated tests."],
  ["Tamper-evident audit ledger", "Audit events are hash-chained, with a verification endpoint."],
  [
    "Signed evidence packs",
    <>
      Packs are signed with RS256 and verifiable against our public JWKS.{" "}
      <a href={`${docs}/VERIFY_EVIDENCE_PACK.md`} target="_blank" rel="noopener noreferrer">
        How to verify a pack
      </a>
      .
    </>,
  ],
  ["MFA", "Multi-factor authentication on every administrative account (cloud, DNS, code hosting, payments)."],
  ["Dependency and secret scanning", "Dependabot updates and gitleaks secret scanning on every change."],
  ["Rate limiting", "Sign-in, sign-up and public endpoints are rate limited."],
  ["No LLM on your content", "No large language model processes customer content by default."],
  ["Backups", "Nightly backups with a monthly restore drill. Recovery point objective 24 hours, recovery time objective 4 hours."],
];

export default function SecurityPage() {
  return (
    <LegalPage title={title} description={description} path={path}>
      <LegalSection title="Our position">
        <p>
          We are not SOC 2 certified. We will pursue SOC 2 Type I when a customer contract requires it. We answer security
          questionnaires on request: email <a href={`mailto:${siteConfig.securityEmail}`}>{siteConfig.securityEmail}</a>.
        </p>
      </LegalSection>

      <LegalSection title="Controls">
        <dl className="divide-y divide-line rounded-xl border border-line bg-white text-sm">
          {controls.map(([name, detail]) => (
            <div key={name} className="grid gap-1 px-4 py-3 sm:grid-cols-[14rem_1fr] sm:gap-4">
              <dt className="font-semibold text-ink">{name}</dt>
              <dd className="text-ink/85">{detail}</dd>
            </div>
          ))}
        </dl>
      </LegalSection>

      <LegalSection title="Subprocessors">
        <p>
          See the <Link href="/subprocessors">subprocessors list</Link> and the <Link href="/dpa">DPA</Link>.
        </p>
      </LegalSection>

      <LegalSection title="Report a vulnerability">
        <p>
          Email <a href={`mailto:${siteConfig.securityEmail}`}>{siteConfig.securityEmail}</a>. Please give us 90 days to fix
          an issue before publishing it, and do not access other customers&apos; data. Our{" "}
          <a href="/.well-known/security.txt">security.txt</a> has the same details.
        </p>
        {siteConfig.statusUrl ? (
          <p>
            Service status:{" "}
            <a href={siteConfig.statusUrl} target="_blank" rel="noopener noreferrer">
              status page
            </a>
            .
          </p>
        ) : null}
      </LegalSection>
    </LegalPage>
  );
}
