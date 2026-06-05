"use client";

import { useTheme } from "next-themes";
import { Button } from "@/components/ui/button";
import { Moon, Sun, Download, RefreshCw } from "lucide-react";

interface HeaderProps {
  title: string;
  subtitle?: string;
  onExportPack: () => void;
  onRunControls: () => void;
}

export function Header({ title, subtitle, onExportPack, onRunControls }: HeaderProps) {
  const { theme, setTheme } = useTheme();

  return (
    <header className="flex justify-between items-start gap-6 mb-6">
      <div>
        <h1 className="text-xl font-semibold tracking-tight">{title}</h1>
        {subtitle && <p className="text-xs text-muted-foreground mt-1 leading-relaxed max-w-lg">{subtitle}</p>}
      </div>
      <div className="flex items-center gap-2 flex-shrink-0 pt-0.5">
        <button
          onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
          aria-label="Toggle theme"
          className="w-8 h-8 rounded-lg flex items-center justify-center text-muted-foreground hover:text-foreground hover:bg-muted transition-colors"
        >
          {theme === "dark" ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
        </button>
        <Button variant="outline" size="sm" className="h-8 text-xs font-medium" onClick={onExportPack}>
          <Download className="h-3.5 w-3.5 mr-1.5" />
          Export pack
        </Button>
        <Button size="sm" className="h-8 text-xs font-medium" onClick={onRunControls}>
          <RefreshCw className="h-3.5 w-3.5 mr-1.5" />
          Refresh
        </Button>
      </div>
    </header>
  );
}
