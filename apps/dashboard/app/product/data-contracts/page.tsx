import { ContractHealthMock } from "@/components/marketing/mockups";
import { ProductDetailPage } from "@/components/marketing/product-detail";
import { marketingMetadata } from "@/lib/seo";

const path = "/product/data-contracts";
const description =
  "Monitor schema and distribution drift on the data your AI features depend on. An open breach automatically blocks promotion.";

export const metadata = marketingMetadata({
  title: "Data contracts",
  description,
  path,
  keywords: ["data contracts", "schema drift", "AI data governance", "EU AI Act Article 10"],
});

export default function DataContractsPage() {
  return (
    <ProductDetailPage
      tone="dark"
      path={path}
      name="Data contracts"
      eyebrow="Data contracts"
      title="Catch upstream changes before they break your release."
      description={description}
      visual={<ContractHealthMock />}
      primary={{ href: "/product/release-gate", label: "See the release gate" }}
      pointsTitle="Data governance the release can see."
      points={[
        { title: "Schema and semantic contracts", body: "Agree what each input should look like, and record when it stops matching." },
        { title: "Drift with severity", body: "Drift events are logged with severity. Only breach-level events block a release." },
        { title: "Breach blocks release", body: "An open breach on an upstream contract stops promotion until it is closed." },
        { title: "Lineage", body: "See which sources feed which systems, so you know who to call." },
        { title: "Remediation history", body: "Closing a breach is recorded, with who closed it and when." },
        { title: "Data governance evidence", body: "Records that support the EU AI Act's data and data governance expectations." },
      ]}
    />
  );
}
