"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";

const NAV_ITEMS = [
  { href: "/command", label: "Command", num: "01" },
  { href: "/systems", label: "Systems", num: "02" },
  { href: "/evidence", label: "Evidence", num: "03" },
  { href: "/evals", label: "Evals", num: "04" },
  { href: "/contracts", label: "Contracts", num: "05" },
  { href: "/audit", label: "Audit", num: "06" },
];

interface SidebarProps {
  blockedCount: number;
}

export function Sidebar({ blockedCount }: SidebarProps) {
  const pathname = usePathname();

  return (
    <aside className="fixed inset-y-0 left-0 w-64 border-r border-border bg-card flex flex-col gap-6 p-5 z-10">
      <div className="flex items-center gap-3">
        <div className="w-10 h-10 rounded-lg bg-cyan-900 dark:bg-cyan-400 grid place-items-center text-white dark:text-cyan-950 font-black text-sm">
          EA
        </div>
        <div>
          <p className="font-bold text-sm leading-tight">EU AI Assurance</p>
          <p className="text-xs text-muted-foreground mt-0.5">Governance control plane</p>
        </div>
      </div>

      <nav className="flex flex-col gap-1">
        {NAV_ITEMS.map((item) => {
          const active = pathname === item.href || pathname.startsWith(`${item.href}/`);
          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                "flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-semibold transition-colors",
                active
                  ? "bg-accent text-accent-foreground shadow-[inset_3px_0_0_hsl(var(--primary))]"
                  : "text-muted-foreground hover:bg-accent hover:text-accent-foreground"
              )}
            >
              <span className="w-6 text-xs text-muted-foreground">{item.num}</span>
              {item.label}
            </Link>
          );
        })}
      </nav>

      <div className="mt-auto border border-border rounded-lg p-3.5 bg-muted/40">
        <span className="inline-block w-2.5 h-2.5 rounded-full bg-red-500 shadow-[0_0_0_5px_color-mix(in_srgb,_theme(colors.red.500),_transparent_80%)]" />
        <p className="text-xs text-muted-foreground mt-2 mb-1">Release gate active</p>
        <p className="text-xl font-bold">{blockedCount} blocked</p>
      </div>
    </aside>
  );
}
