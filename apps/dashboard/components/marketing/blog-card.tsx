import Image from "next/image";
import Link from "next/link";
import { ArrowRight } from "lucide-react";

import { EuFlag } from "@/components/marketing/brand-icons";
import { categoryLabel, formatPostDate, type BlogPost } from "@/lib/blog";
import { cn } from "@/lib/utils";

/** Cover photo, or category artwork until a photo is supplied. */
export function BlogCover({ post, className, priority }: { post: BlogPost; className?: string; priority?: boolean }) {
  return (
    <div className={cn("relative overflow-hidden bg-navy-900", className)}>
      {post.cover ? (
        <Image
          src={post.cover}
          alt={post.coverAlt}
          fill
          priority={priority}
          sizes="(min-width: 1024px) 33vw, 100vw"
          className="object-cover"
        />
      ) : post.category === "engineering" ? (
        <div className="mk-grid absolute inset-0 bg-navy-950" aria-hidden="true">
          <pre className="absolute inset-x-6 top-6 font-mono text-[11px] leading-5 text-periwinkle/70">
            {"jobs:\n  gate:\n    steps:\n      - run: curl … release-gate\n        # PASS=0 BLOCKED=1 REVIEW=2"}
          </pre>
        </div>
      ) : (
        <div
          className="absolute inset-0 flex items-center justify-center bg-gradient-to-br from-[#1d3fbf] via-[#1a3399] to-navy-950"
          aria-hidden="true"
        >
          <EuFlag className="h-16 w-24 opacity-90 drop-shadow-xl" />
        </div>
      )}
    </div>
  );
}

/** Storyboard frame 11. */
export function BlogCard({ post }: { post: BlogPost }) {
  return (
    <article className="group flex w-full flex-col overflow-hidden rounded-2xl border border-line bg-white transition-shadow hover:shadow-xl hover:shadow-navy-950/5">
      <BlogCover post={post} className="aspect-[16/9]" />
      <div className="flex flex-1 flex-col p-5">
        <p className="text-xs font-bold uppercase tracking-[0.08em] text-brand">{categoryLabel[post.category]}</p>
        <h3 className="mt-2 text-lg font-bold leading-snug tracking-tight text-ink">
          <Link href={`/blog/${post.slug}`} className="after:absolute after:inset-0 focus-visible:outline-none">
            <span className="relative">{post.title}</span>
          </Link>
        </h3>
        <p className="mt-2 line-clamp-3 flex-1 text-sm text-ink-muted">{post.excerpt}</p>
        <p className="mt-4 flex items-center justify-between text-xs text-ink-muted">
          <span>
            <time dateTime={post.published}>{formatPostDate(post.published)}</time> · {post.readMinutes} min read
          </span>
          <ArrowRight className="h-4 w-4 text-brand transition-transform group-hover:translate-x-0.5" aria-hidden="true" />
        </p>
      </div>
    </article>
  );
}
