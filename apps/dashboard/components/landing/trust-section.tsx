import { Badge } from "@/components/ui/badge";
import { trustBadges } from "@/lib/landing-content";

export function TrustSection() {
  return (
    <section aria-label="Trust and compliance" className="border-y border-border bg-muted/40">
      <div className="mx-auto max-w-6xl px-4 py-10 sm:px-6">
        <div className="flex flex-wrap items-center justify-center gap-3">
          {trustBadges.map((badge) => (
            <Badge key={badge} variant="outline" className="px-3 py-1 text-xs">
              {badge}
            </Badge>
          ))}
        </div>
      </div>
    </section>
  );
}
