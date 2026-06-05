"use client";

import { useTheme } from "next-themes";
import { Button } from "@/components/ui/button";
import { Moon, Sun, Download, Play } from "lucide-react";

interface HeaderProps {
  onExportPack: () => void;
  onRunControls: () => void;
}

export function Header({ onExportPack, onRunControls }: HeaderProps) {
  const { theme, setTheme } = useTheme();

  return (
    <header className="flex justify-between items-start gap-6 mb-6">
      <div>
        <p className="text-xs font-black uppercase tracking-wider text-cyan-700 dark:text-cyan-400 mb-1.5">
          EU AI Act + GDPR + operational controls
        </p>
        <h1 className="text-3xl font-bold leading-tight">
          Assure high-risk AI systems before they reach production.
        </h1>
      </div>
      <div className="flex items-center gap-2 flex-shrink-0 pt-1">
        <Button
          variant="outline"
          size="icon"
          onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
          aria-label="Toggle theme"
        >
          {theme === "dark" ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
        </Button>
        <Button variant="outline" onClick={onExportPack}>
          <Download className="h-4 w-4 mr-2" />
          Export evidence pack
        </Button>
        <Button onClick={onRunControls}>
          <Play className="h-4 w-4 mr-2" />
          Run controls
        </Button>
      </div>
    </header>
  );
}
