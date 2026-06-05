"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";
import {
  LayoutDashboard,
  Server,
  FileSearch,
  FlaskConical,
  GitBranch,
  ScrollText,
  ShieldCheck,
} from "lucide-react";

const NAV_ITEMS = [
  { href: "/command", label: "Dashboard", icon: LayoutDashboard },
  { href: "/systems", label: "AI Systems", icon: Server },
  { href: "/evidence", label: "Evidence", icon: FileSearch },
  { href: "/evals", label: "Eval Gates", icon: FlaskConical },
  { href: "/contracts", label: "Contracts", icon: GitBranch },
  { href: "/audit", label: "Audit Log", icon: ScrollText },
];

interface SidebarProps {
  blockedCount: number;
}

export function Sidebar({ blockedCount }: SidebarProps) {
  const pathname = usePathname();

  return (
    <aside className="fixed inset-y-0 left-0 w-56 border-r border-border bg-card flex flex-col z-10">
      {/* Logo */}
      <div className="flex items-center gap-2.5 px-5 h-14 border-b border-border flex-shrink-0">
        <div className="w-7 h-7 rounded-lg bg-primary grid place-items-center flex-shrink-0">
          <ShieldCheck className="w-3.5 h-3.5 text-primary-foreground" />
        </div>
        <span className="font-semibold text-sm">EU AI Assurance</span>
      </div>

      {/* Navigation */}
      <nav className="flex flex-col px-3 py-4 flex-1 overflow-y-auto">
        <p className="text-[10px] font-semibold uppercase tracking-widest text-muted-foreground px-2 mb-2">
          Navigation
        </p>
        {NAV_ITEMS.map((item) => {
          const active = pathname === item.href || pathname.startsWith(`${item.href}/`);
          const Icon = item.icon;
          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                "flex items-center gap-2.5 px-2.5 py-2 text-sm rounded-lg transition-colors mb-0.5",
                active
                  ? "bg-accent text-primary font-medium"
                  : "text-muted-foreground hover:text-foreground hover:bg-muted/50 font-normal"
              )}
            >
              <Icon className={cn("w-4 h-4 flex-shrink-0", active ? "text-primary" : "text-muted-foreground/70")} />
              {item.label}
            </Link>
          );
        })}
      </nav>

      {/* Bottom status */}
      <div className="px-3 pb-5 flex-shrink-0">
        <div
          className={cn(
            "rounded-xl p-3 border",
            blockedCount > 0
              ? "bg-red-50 border-red-100 dark:bg-red-950/30 dark:border-red-900"
              : "bg-emerald-50 border-emerald-100 dark:bg-emerald-950/30 dark:border-emerald-900"
          )}
        >
          <p className={cn("text-[11px] font-medium", blockedCount > 0 ? "text-red-600 dark:text-red-400" : "text-emerald-600 dark:text-emerald-400")}>
            Release Gate
          </p>
          <p className={cn("text-xs font-semibold mt-0.5 truncate", blockedCount > 0 ? "text-red-800 dark:text-red-300" : "text-emerald-800 dark:text-emerald-300")}>
            {blockedCount > 0 ? `${blockedCount} blocked` : "All systems clear"}
          </p>
        </div>
      </div>
    </aside>
  );
}
