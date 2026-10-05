import type { Metadata } from "next";
import Link from "next/link";

import { CoverTable, LegalPage, LegalSection } from "@/components/landing/legal-page-shell";
import { COMMON_PAPER, DPA_VERSION, LEGAL } from "@/lib/legal";
import { siteConfig } from "@/lib/site-config";

const title = "Data Processing Agreement";
const description = `The ${siteConfig.shortName} DPA: the Common Paper DPA Standard Terms with our Cover Page, EU SCCs and the UK Addendum.`;
const path = "/dpa";

export const metadata: Metadata = { title, description, alternates: { canonical: path } };

export default function DpaPage() {
  return (
    <LegalPage title={title} description={description} path={path}>
      <LegalSection title="Agreement">
        <p>
          The{" "}
          <a href={COMMON_PAPER.dpa.url} target="_blank" rel="noopener noreferrer">
            {COMMON_PAPER.dpa.name}, version {COMMON_PAPER.dpa.version}
          </a>{" "}
          ({COMMON_PAPER.license.name}) apply, with this Cover Page. Cover Page version: <code>{DPA_VERSION}</code>.
        </p>
      </LegalSection>

      <LegalSection title="Cover Page">
        <CoverTable
          rows={[
            ["Provider", LEGAL.provider],
            ["Customer", "The organisation that holds the workspace"],
            ["Agreement", <Link key="t" href="/terms">Terms of Service</Link>],
            ["Subject matter", `Hosting the ${siteConfig.shortName} service`],
            ["Duration", "The term of the Agreement plus 30 days"],
            ["Nature and purpose", "Storage, indexing and retrieval of workspace content to provide the service"],
            ["Data subjects", "Customer's staff, and any individuals named in uploaded evidence"],
            ["Categories of personal data", "Contact data; the content of uploaded documents"],
            ["Special categories", "None intended; Customer should not upload them"],
            ["Approved subprocessors", <Link key="s" href="/subprocessors">Subprocessors list</Link>],
            ["Security Policy", <Link key="sec" href="/security">Security</Link>],
            [
              "Restricted transfers",
              "EU Standard Contractual Clauses Module Two (Customer as controller) and Module Three (Customer as processor), and the UK Addendum, are incorporated",
            ],
            ["Governing Member State (SCCs)", "Ireland"],
            ["Provider contact", LEGAL.noticeEmail],
          ]}
        />
      </LegalSection>

      <LegalSection title="Accepting the DPA">
        <p>
          Admins accept this DPA on behalf of their organisation in <strong>Settings → Legal</strong>. We record the
          accepted version and the date. Enterprise customers can sign it with their order form instead.
        </p>
      </LegalSection>
    </LegalPage>
  );
}
