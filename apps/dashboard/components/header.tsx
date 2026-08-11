"use client";

import { useEffect, useState } from "react";
import { useTheme } from "@/components/theme-provider";
import { Button } from "@/components/ui/button";
import { Moon, Sun, FileJson, FileText, RefreshCw } from "lucide-react";

interface HeaderProps {
  title: string;
  subtitle?: string;
  onExportJson: () => void;
  onExportPdf: () => void;
  onRunControls: () => void;
  exportBusy?: boolean;
}

export function Header({
  title,
  subtitle,
  onExportJson,
  onExportPdf,
  onRunControls,
  exportBusy,
}: HeaderProps) {
  const { resolvedTheme, setTheme } = useTheme();
  // Theme is applied from localStorage after mount — keep icon stable on SSR.
  const [mounted, setMounted] = useState(false);
  useEffect(() => setMounted(true), []);
  const isDark = mounted && resolvedTheme === "dark";

  return (
    <header className="flex justify-between items-start gap-6 mb-6">
      <div>
        <h1 className="text-xl font-semibold tracking-tight">{title}</h1>
        {subtitle && <p className="text-xs text-muted-foreground mt-1 leading-relaxed max-w-lg">{subtitle}</p>}
      </div>
      <div className="flex items-center gap-2 flex-shrink-0 pt-0.5">
        <button
          onClick={() => setTheme(isDark ? "light" : "dark")}
          aria-label="Toggle theme"
          className="w-8 h-8 rounded-lg flex items-center justify-center text-muted-foreground hover:text-foreground hover:bg-muted transition-colors"
        >
          {isDark ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
        </button>
        <Button
          variant="outline"
          size="sm"
          className="h-8 text-xs font-medium"
          onClick={onExportJson}
          disabled={exportBusy}
        >
          <FileJson className="h-3.5 w-3.5 mr-1.5" />
          Export JSON
        </Button>
        <Button
          variant="outline"
          size="sm"
          className="h-8 text-xs font-medium"
          onClick={onExportPdf}
          disabled={exportBusy}
        >
          <FileText className="h-3.5 w-3.5 mr-1.5" />
          Export PDF
        </Button>
        <Button size="sm" className="h-8 text-xs font-medium" onClick={onRunControls}>
          <RefreshCw className="h-3.5 w-3.5 mr-1.5" />
          Refresh
        </Button>
      </div>
    </header>
  );
}
