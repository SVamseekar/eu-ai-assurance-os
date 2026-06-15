import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { capabilities } from "@/lib/landing-content";

export function CapabilitiesSection() {
  return (
    <section id="capabilities" aria-label="Product capabilities" className="mx-auto max-w-6xl px-4 py-16 sm:px-6">
      <h2 className="font-heading text-2xl font-semibold tracking-tight sm:text-3xl">
        Everything a release decision needs, in one place
      </h2>
      <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {capabilities.map((capability) => {
          const Icon = capability.icon;
          return (
            <Card key={capability.title}>
              <CardHeader>
                <Icon className="h-5 w-5 text-primary" aria-hidden="true" />
                <CardTitle className="mt-2">{capability.title}</CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground">
                {capability.description}
              </CardContent>
            </Card>
          );
        })}
      </div>
    </section>
  );
}
