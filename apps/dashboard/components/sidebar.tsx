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
  ShieldAlert,
} from "lucide-react";

const NAV_ITEMS = [
  { href: "/command", label: "Command", icon: LayoutDashboard },
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
    <aside className="fixed inset-y-0 left-0 w-60 border-r border-border bg-card flex flex-col z-10">
      {/* Logo */}
      <div className="flex items-center gap-3 px-5 py-5 border-b border-border">
        <div className="w-8 h-8 rounded-lg bg-primary grid place-items-center flex-shrink-0">
          <ShieldAlert className="w-4 h-4 text-primary-foreground" />
        </div>
        <div className="min-w-0">
          <p className="font-semibold text-sm leading-tight truncate">EU AI Assurance</p>
          <p className="text-[11px] text-muted-foreground leading-tight mt-0.5">Governance OS</p>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex flex-col gap-0.5 px-3 py-4 flex-1">
        <p className="text-[10px] font-semibold uppercase tracking-wider text-muted-foreground px-2 mb-2">
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
                "flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors",
                active
                  ? "bg-accent text-primary font-semibold"
                  : "text-muted-foreground hover:text-foreground hover:bg-muted/60 font-medium"
              )}
            >
              <Icon
                className={cn("w-4 h-4 flex-shrink-0", active ? "text-primary" : "text-muted-foreground")}
              />
              {item.label}
            </Link>
          );
        })}
      </nav>

      {/* Release gate status */}
      <div className="px-3 pb-4">
        <div
          className={cn(
            "rounded-xl px-4 py-3 border",
            blockedCount > 0
              ? "bg-red-50 border-red-200 dark:bg-red-950/40 dark:border-red-800"
              : "bg-emerald-50 border-emerald-200 dark:bg-emerald-950/40 dark:border-emerald-800"
          )}
        >
          <div className="flex items-center gap-2 mb-1">
            <span
              className={cn(
                "inline-block w-2 h-2 rounded-full flex-shrink-0",
                blockedCount > 0 ? "bg-red-500" : "bg-emerald-500"
              )}
            />
            <span
              className={cn(
                "text-xs font-semibold",
                blockedCount > 0 ? "text-red-700 dark:text-red-400" : "text-emerald-700 dark:text-emerald-400"
              )}
            >
              Release Gate
            </span>
          </div>
          {blockedCount > 0 ? (
            <p className="text-sm font-bold text-red-800 dark:text-red-300">
              {blockedCount} system{blockedCount !== 1 ? "s" : ""} blocked
            </p>
          ) : (
            <p className="text-sm font-bold text-emerald-800 dark:text-emerald-300">All systems clear</p>
          )}
        </div>
      </div>
    </aside>
  );
}
