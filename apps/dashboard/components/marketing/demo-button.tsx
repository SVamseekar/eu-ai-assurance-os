"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { Play } from "lucide-react";

import { mkButton } from "@/components/marketing/primitives";
import { cn } from "@/lib/utils";

/** Opens the shared read-only demo workspace, then lands on the command view. */
export function DemoButton({
  variant = "outlineOnDark",
  className,
}: {
  variant?: keyof typeof mkButton;
  className?: string;
}) {
  const router = useRouter();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function start() {
    setBusy(true);
    setError(null);
    try {
      const res = await fetch("/api/auth/demo", { method: "POST" });
      if (res.ok) {
        router.push("/command");
        return;
      }
    } catch {
      // Network failure falls through to the same message.
    }
    setError("The demo is busy. Try again in a minute.");
    setBusy(false);
  }

  return (
    <div className="flex flex-col">
      <button type="button" className={cn(mkButton[variant], className)} onClick={start} disabled={busy}>
        <Play className="h-4 w-4" aria-hidden="true" />
        {busy ? "Opening demo…" : "Try the live demo"}
      </button>
      {error ? (
        <p role="alert" className="mt-2 text-xs text-[#f87171]">
          {error}
        </p>
      ) : null}
    </div>
  );
}
