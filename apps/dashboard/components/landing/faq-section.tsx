import { faqItems } from "@/lib/landing-content";

export function FaqSection() {
  return (
    <section id="faq" aria-label="Frequently asked questions" className="mx-auto max-w-3xl px-4 py-16 sm:px-6">
      <h2 className="font-heading text-2xl font-semibold tracking-tight sm:text-3xl">
        Frequently asked questions
      </h2>
      <div className="mt-8 space-y-3">
        {faqItems.map((item) => (
          <details key={item.question} className="group rounded-lg border border-border p-4">
            <summary className="cursor-pointer font-heading text-sm font-semibold">
              {item.question}
            </summary>
            <p className="mt-2 text-sm text-muted-foreground">{item.answer}</p>
          </details>
        ))}
      </div>
    </section>
  );
}
