import { notFound } from "next/navigation";

import { MarketingPageShell } from "@/components/landing/marketing-page-shell";
import { BlogCard, BlogCover } from "@/components/marketing/blog-card";
import { Band, Container, PageHeader } from "@/components/marketing/primitives";
import { CtaBand } from "@/components/marketing/sections";
import { categoryLabel, formatPostDate, postBySlug, sortedPosts } from "@/lib/blog";
import { marketingMetadata } from "@/lib/seo";
import { siteConfig } from "@/lib/site-config";

type Params = { params: Promise<{ slug: string }> };

export const dynamicParams = false;

export function generateStaticParams() {
  return sortedPosts().map((p) => ({ slug: p.slug }));
}

export async function generateMetadata({ params }: Params) {
  const post = postBySlug((await params).slug);
  if (!post) return {};
  return marketingMetadata({ title: post.title, description: post.excerpt, path: `/blog/${post.slug}` });
}

export default async function BlogPostPage({ params }: Params) {
  const post = postBySlug((await params).slug);
  if (!post) notFound();
  const url = `${siteConfig.url}/blog/${post.slug}`;
  const more = sortedPosts().filter((p) => p.slug !== post.slug).slice(0, 3);

  return (
    <MarketingPageShell
      jsonLd={{
        "@context": "https://schema.org",
        "@type": "BlogPosting",
        headline: post.title,
        description: post.excerpt,
        datePublished: post.published,
        url,
        author: { "@type": "Organization", name: siteConfig.shortName },
        publisher: { "@type": "Organization", name: siteConfig.shortName },
      }}
    >
      <Band tone="light" muted className="border-b border-line">
        <Container className="pt-10 pb-12 sm:pt-12">
          <PageHeader
            tone="light"
            eyebrow={categoryLabel[post.category]}
            title={post.title}
            description={post.excerpt}
            crumbs={[
              { href: "/", label: "Home" },
              { href: "/blog", label: "Blog" },
              { href: `/blog/${post.slug}`, label: categoryLabel[post.category] },
            ]}
          >
            <p className="mt-5 text-sm text-ink-muted">
              <time dateTime={post.published}>{formatPostDate(post.published)}</time> · {post.readMinutes} min read
            </p>
          </PageHeader>
        </Container>
      </Band>

      <Band tone="light">
        <Container className="py-12 sm:py-16">
          <BlogCover post={post} priority className="mx-auto aspect-[21/9] max-w-4xl rounded-2xl" />
          <article className="mx-auto mt-12 max-w-2xl space-y-5 text-[17px] leading-relaxed text-ink/90">
            {post.body.map((block, i) => {
              switch (block.type) {
                case "h2":
                  return (
                    <h2 key={i} className="pt-4 text-2xl font-bold tracking-tight text-ink">
                      {block.text}
                    </h2>
                  );
                case "ul":
                  return (
                    <ul key={i} className="list-disc space-y-2 pl-6 marker:text-brand">
                      {block.items.map((item) => (
                        <li key={item}>{item}</li>
                      ))}
                    </ul>
                  );
                case "code":
                  return (
                    <pre
                      key={i}
                      className="overflow-x-auto rounded-xl bg-navy-950 p-5 font-mono text-[13px] leading-6 text-[#c9d4ff]"
                    >
                      <code>{block.text}</code>
                    </pre>
                  );
                default:
                  return <p key={i}>{block.text}</p>;
              }
            })}
            {post.sources.length ? (
              <aside className="mt-10 rounded-xl border border-line bg-mist p-5 text-sm">
                <h2 className="font-semibold text-ink">Sources</h2>
                <ul className="mt-2 space-y-1">
                  {post.sources.map((s) => (
                    <li key={s.url}>
                      <a href={s.url} target="_blank" rel="noopener noreferrer" className="text-brand hover:underline">
                        {s.title}
                      </a>
                    </li>
                  ))}
                </ul>
              </aside>
            ) : null}
            <p className="text-sm text-ink-muted">
              This article is general information, not legal advice. Your counsel decides how the law applies to your
              systems.
            </p>
          </article>
        </Container>
      </Band>

      {more.length ? (
        <Band tone="light" muted className="border-t border-line">
          <Container className="py-16">
            <h2 className="text-2xl font-bold tracking-tight text-ink">More from the blog</h2>
            <ul className="mt-8 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
              {more.map((p) => (
                <li key={p.slug} className="relative flex">
                  <BlogCard post={p} />
                </li>
              ))}
            </ul>
          </Container>
        </Band>
      ) : null}

      <CtaBand />
    </MarketingPageShell>
  );
}
