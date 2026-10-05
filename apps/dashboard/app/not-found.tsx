import { MarketingPageShell } from "@/components/landing/marketing-page-shell";
import { Band, ButtonLink, Container, PageHeader } from "@/components/marketing/primitives";

export const metadata = { title: "Page not found", robots: { index: false, follow: false } };

export default function NotFound() {
  return (
    <MarketingPageShell>
      <Band tone="light" muted>
        <Container className="pt-16 pb-28 sm:pt-24">
          <PageHeader
            tone="light"
            eyebrow="404"
            title="This page could not be found."
            description="The link may be out of date, or the page has moved."
          >
            <div className="mt-8 flex flex-wrap gap-3">
              <ButtonLink href="/" variant="primary" arrow>
                Go to the home page
              </ButtonLink>
              <ButtonLink href="/product" variant="outline">
                Explore the product
              </ButtonLink>
            </div>
          </PageHeader>
        </Container>
      </Band>
    </MarketingPageShell>
  );
}
