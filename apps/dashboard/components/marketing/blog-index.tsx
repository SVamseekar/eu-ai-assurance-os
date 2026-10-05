"use client";

import { useEffect, useState } from "react";

import { BlogCard } from "@/components/marketing/blog-card";
import { categoryLabel, type BlogCategory, type BlogPost } from "@/lib/blog";
import { cn } from "@/lib/utils";

type Filter = BlogCategory | "all";

const pluralLabel: Record<BlogCategory, string> = {
  guide: "Guides",
  update: "Updates",
  engineering: "Engineering",
  "case-study": "Case studies",
};

/** Category filter for the blog index. `?category=guide` preselects a tab (used by the Resources menu). */
export function BlogIndex({ posts }: { posts: BlogPost[] }) {
  const categories = (Object.keys(categoryLabel) as BlogCategory[]).filter((c) => posts.some((p) => p.category === c));
  const [filter, setFilter] = useState<Filter>("all");

  useEffect(() => {
    const requested = new URLSearchParams(window.location.search).get("category");
    if (requested && categories.includes(requested as BlogCategory)) setFilter(requested as BlogCategory);
    // Categories derive from static posts, so reading the URL once on mount is enough.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function choose(next: Filter) {
    setFilter(next);
    const url = next === "all" ? "/blog" : `/blog?category=${next}`;
    window.history.replaceState(null, "", url);
  }

  const visible = filter === "all" ? posts : posts.filter((p) => p.category === filter);

  return (
    <div>
      <div role="group" aria-label="Filter by category" className="flex flex-wrap gap-2">
        {(["all", ...categories] as Filter[]).map((value) => (
          <button
            key={value}
            type="button"
            aria-pressed={filter === value}
            onClick={() => choose(value)}
            className={cn(
              "rounded-lg px-4 py-2 text-sm font-semibold transition-colors",
              filter === value ? "bg-brand text-white" : "text-ink-muted hover:bg-mist hover:text-ink",
            )}
          >
            {value === "all" ? "All" : pluralLabel[value]}
          </button>
        ))}
      </div>
      <ul className="mt-8 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {visible.map((post) => (
          <li key={post.slug} className="relative flex">
            <BlogCard post={post} />
          </li>
        ))}
      </ul>
    </div>
  );
}
