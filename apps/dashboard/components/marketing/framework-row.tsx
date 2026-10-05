import { FRAMEWORKS } from "@/lib/frameworks";
import { cn } from "@/lib/utils";

/** Framework coverage as badges; anything not shipped carries a "coming soon" suffix. */
export function FrameworkRow({ className }: { className?: string }) {
  return (
    <ul className={cn("flex flex-wrap gap-2", className)}>
      {FRAMEWORKS.filter((f) => f.short !== "Digital Omnibus").map((f) => (
        <li
          key={f.short}
          className={cn(
            "rounded-full border px-3 py-1 text-sm",
            f.status === "live" ? "border-pass/30 bg-pass-soft font-semibold text-pass" : "border-line bg-white text-ink-muted",
          )}
        >
          {f.short}
          {f.status === "live" ? "" : " · coming soon"}
        </li>
      ))}
    </ul>
  );
}
