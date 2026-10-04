"use client";

import Link from "next/link";
import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";

/** Shown only in the shared read-only demo workspace. */
export function DemoBanner() {
  const me = useQuery({ queryKey: ["me"], queryFn: api.me, retry: false, staleTime: 5 * 60_000 });
  if (!me.data?.demo) return null;
  return (
    <div role="status" className="mb-4 rounded-lg border border-primary/30 bg-primary/10 px-4 py-2.5 text-sm">
      You&apos;re viewing a read-only demo.{" "}
      <Link href="/signup" className="font-medium underline underline-offset-4">
        Create your workspace
      </Link>
    </div>
  );
}
