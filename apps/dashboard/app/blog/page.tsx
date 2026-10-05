import { MarketingPageShell } from "@/components/landing/marketing-page-shell";
import { BlogIndex } from "@/components/marketing/blog-index";
import { Band, Container, PageHeader } from "@/components/marketing/primitives";
import { sortedPosts } from "@/lib/blog";
import { marketingMetadata, webPageJsonLd } from "@/lib/seo";
import { siteConfig } from "@/lib/site-config";

const title = "Blog";
const description =
  "Insights, guides and updates: practical guidance on the EU AI Act, AI assurance and real-world implementation.";
const path = "/blog";

export const metadata = marketingMetadata({
  title: "Insights, guides and updates",
  description,
  path,
  keywords: ["EU AI Act guide", "AI governance blog", "Digital Omnibus on AI", "AI release gate"],
});

export default function BlogPage() {
  return (
    <MarketingPageShell
      jsonLd={webPageJsonLd({
        name: `${title} — ${siteConfig.shortName}`,
        description,
        path,
        crumbs: [
          { name: "Home", path: "/" },
          { name: title, path },
        ],
      })}
    >
      <Band tone="light">
        <Container className="pt-10 pb-24 sm:pt-12">
          <PageHeader
            tone="light"
            eyebrow="Resources"
            title="Insights, guides and updates."
            description="Practical guidance on the EU AI Act, AI assurance and real-world implementation."
          />
          <div className="mt-10">
            <BlogIndex posts={sortedPosts()} />
          </div>
        </Container>
      </Band>
    </MarketingPageShell>
  );
}
