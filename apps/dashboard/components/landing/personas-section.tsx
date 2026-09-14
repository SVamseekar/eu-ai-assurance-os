import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { personas } from "@/lib/landing-content";

export function PersonasSection({ showHeading = true }: { showHeading?: boolean }) {
  return (
    <section aria-label="Who it's for" className="mx-auto max-w-6xl px-4 py-16 sm:px-6">
      {showHeading ? (
        <h2 className="font-heading text-2xl font-semibold tracking-tight sm:text-3xl">
          Built for everyone in the release path
        </h2>
      ) : null}
      <div className={showHeading ? "mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3" : "grid gap-4 sm:grid-cols-2 lg:grid-cols-3"}>
        {personas.map((persona) => (
          <Card key={persona.role} size="sm">
            <CardHeader>
              <CardTitle>{persona.role}</CardTitle>
            </CardHeader>
            <CardContent className="text-sm text-muted-foreground">{persona.description}</CardContent>
          </Card>
        ))}
      </div>
    </section>
  );
}
