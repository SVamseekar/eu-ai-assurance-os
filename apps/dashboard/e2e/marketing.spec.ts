import { devices, expect, test } from "@playwright/test";

const PAGES = [
  "/",
  "/product",
  "/product/evidence",
  "/product/evaluations",
  "/product/data-contracts",
  "/product/release-gate",
  "/how-it-works",
  "/eu-ai-act",
  "/who-its-for",
  "/pricing",
  "/blog",
  "/request-demo",
  "/faq",
  "/method",
  "/terms",
  "/security",
  "/subprocessors",
  "/tools/ai-act-check",
  "/tools/ai-act-deadlines",
];

test.describe("public site", () => {
  for (const path of PAGES) {
    test(`${path} renders one h1 without console errors`, async ({ page }) => {
      const errors: string[] = [];
      page.on("pageerror", (e) => errors.push(e.message));
      const res = await page.goto(path);
      expect(res?.status()).toBe(200);
      await expect(page.locator("h1")).toHaveCount(1);
      expect(errors).toEqual([]);
    });
  }

  test("mega menu opens on click and closes on Escape", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await page.goto("/");
    const trigger = page.getByRole("button", { name: "Product", exact: true });
    await trigger.click();
    await expect(trigger).toHaveAttribute("aria-expanded", "true");
    await expect(page.getByRole("region", { name: "Product menu" }).getByRole("link", { name: /Release Gate/ })).toBeVisible();
    await page.keyboard.press("Escape");
    await expect(trigger).toHaveAttribute("aria-expanded", "false");
  });

  test("header keeps the page tone when scrolling", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await page.goto("/");
    const header = page.locator("header").first();
    await expect(header).toHaveClass(/bg-navy-950\/70/);
    await page.evaluate(() => window.scrollTo(0, 1200));
    await expect(header).toHaveClass(/bg-navy-950\/95/);
    await expect(header).not.toHaveClass(/bg-white/);
    await page.goto("/pricing");
    await expect(page.locator("header").first()).toHaveClass(/bg-white/);
  });

  test("deadline bar can be dismissed and stays dismissed", async ({ page }) => {
    await page.goto("/");
    const bar = page.getByRole("complementary", { name: "Regulatory deadline" });
    await expect(bar).toBeVisible();
    await bar.getByRole("button", { name: "Dismiss deadline notice" }).click();
    await expect(bar).toHaveCount(0);
    await page.reload();
    await expect(page.getByRole("complementary", { name: "Regulatory deadline" })).toHaveCount(0);
  });

  test("pricing toggle switches to yearly prices", async ({ page }) => {
    await page.goto("/pricing");
    await expect(page.getByText("$79", { exact: true })).toBeVisible();
    await page.getByRole("button", { name: "Yearly" }).click();
    await expect(page.getByText("$790", { exact: true })).toBeVisible();
    await expect(page.getByText("$2,990", { exact: true })).toBeVisible();
    await expect(page.getByRole("link", { name: /Start free trial/ }).first()).toHaveAttribute("href", /interval=yearly/);
  });

  test("pricing answers the plan questions and has no consulting language", async ({ page }) => {
    await page.goto("/pricing");
    for (const q of ["Where is my data stored?", "Do you use my data to train AI?", "Refunds?", "Can I add a VAT or tax ID?"]) {
      await expect(page.getByText(q, { exact: true })).toBeVisible();
    }
    for (const path of ["/pricing", "/order-form", "/msa"]) {
      await page.goto(path);
      const text = await page.locator("main").innerText();
      for (const phrase of ["scoped readiness work", "readout with the people", "written quote", "readiness sprint"]) {
        expect(text, `${path} mentions "${phrase}"`).not.toContain(phrase);
      }
    }
  });

  test("blog filter narrows posts and honours ?category", async ({ page }) => {
    await page.goto("/blog?category=engineering");
    await expect(page.getByRole("button", { name: "Engineering" })).toHaveAttribute("aria-pressed", "true");
    await expect(page.getByRole("article")).toHaveCount(1);
    await page.getByRole("button", { name: "All" }).click();
    await expect(page.getByRole("article")).toHaveCount(3);
  });

  test("solutions links select the matching use-case tab", async ({ page }) => {
    await page.goto("/who-its-for#legal");
    await expect(page.getByRole("tab", { name: /Legal/ })).toHaveAttribute("aria-selected", "true");
    await expect(page.getByRole("heading", { name: "For legal counsel" })).toBeVisible();
    await page.getByRole("tab", { name: /Legal/ }).press("ArrowRight");
    await expect(page.getByRole("tab", { name: /Data/ })).toHaveAttribute("aria-selected", "true");
  });

  test("solutions menu switches the tab when already on the use-cases page", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await page.goto("/who-its-for");
    await expect(page.getByRole("tab", { name: "Engineering" })).toHaveAttribute("aria-selected", "true");
    for (const [name, heading] of [
      ["Data", /For data/],
      ["Legal", "For legal counsel"],
    ] as const) {
      await page.getByRole("button", { name: "Solutions", exact: true }).click();
      await page.getByRole("region", { name: "Solutions menu" }).getByRole("link", { name: new RegExp(`^${name}`) }).click();
      await expect(page.getByRole("tab", { name })).toHaveAttribute("aria-selected", "true");
      await expect(page.getByRole("heading", { level: 3, name: heading })).toBeVisible();
    }
  });

  test("demo request form asks for four details", async ({ page }) => {
    await page.goto("/request-demo");
    const form = page.locator("form");
    for (const label of [/^Work email/, /^Company/, /^Role/, /^How can we help\?/]) {
      await expect(form.getByLabel(label)).toBeVisible();
    }
    await expect(page.getByRole("button", { name: "Request a demo" })).toBeVisible();
  });

  test("code tabs switch samples", async ({ page }) => {
    await page.goto("/product/release-gate");
    await page.getByRole("tab", { name: "cURL" }).click();
    await expect(page.getByRole("tabpanel")).toContainText("X-Api-Key");
    await page.getByRole("tab", { name: "API response" }).click();
    await expect(page.getByRole("tabpanel")).toContainText('"exitCode": 0');
  });

  test("home CTAs point at signup and the enterprise demo", async ({ page }) => {
    await page.goto("/");
    await expect(page.getByRole("link", { name: "Start free" }).first()).toHaveAttribute("href", "/signup");
    await expect(page.getByRole("link", { name: "Book an enterprise demo" }).first()).toHaveAttribute("href", "/request-demo");
    await expect(page.getByRole("button", { name: "Try the live demo" }).first()).toBeVisible();
  });

  test("the live demo button opens the read-only demo workspace", async ({ page, request }) => {
    // Needs the API with ASSURANCE_DEMO_ENABLED=true, as in the CI E2E job.
    const probe = await request.post("/api/auth/demo").catch(() => null);
    test.skip(!probe || !probe.ok(), "demo workspace not enabled on this API");
    await page.context().clearCookies();
    await page.goto("/");
    await page.getByRole("button", { name: "Try the live demo" }).first().click();
    await expect(page).toHaveURL(/\/command/);
    await expect(page.getByText(/read-only demo/i)).toBeVisible();
  });

  test("home lists every regulatory deadline with its source", async ({ page }) => {
    await page.goto("/");
    for (const label of ["Article 50(2) marking", "Automated-decision transparency", "Automated decision-making technology"]) {
      await expect(page.getByText(label).first()).toBeVisible();
    }
    await expect(page.locator('a[href="https://leg.colorado.gov/bills/sb26-189"]').first()).toBeVisible();
  });

  test("the free AI Act check works without signing in and stores nothing", async ({ page, request }) => {
    const probe = await request.get("/api/public/determination").catch(() => null);
    test.skip(!probe || !probe.ok(), "API not reachable");
    await page.context().clearCookies();
    await page.goto("/tools/ai-act-check");
    const form = page.locator("form");
    // A credit-scoring system: finance, eligibility decisions, profiling, essential private service.
    const answers: Record<string, string> = {
      operator_role: "provider",
      sector: "finance",
      users_affected: "many",
      decision_impact: "eligibility",
      essential_private_service: "true",
      profiling: "true",
      interacts_with_natural_persons: "false",
    };
    const selects = form.locator("select[required]");
    await expect(selects.first()).toBeVisible();
    for (const name of await selects.evaluateAll((els) => els.map((el) => (el as HTMLSelectElement).name))) {
      await form.locator(`select[name="${name}"]`).selectOption(answers[name] ?? "false");
    }
    await form.getByRole("button", { name: "Check my AI system" }).click();
    await expect(page.getByText("Likely high-risk")).toBeVisible();
    await expect(page.getByText("not legal advice").first()).toBeVisible();
    await expect(page.getByRole("link", { name: /Start free/ }).last()).toHaveAttribute("href", "/signup");
  });

  test("the deadline calendar downloads an .ics file", async ({ page }) => {
    await page.goto("/tools/ai-act-deadlines");
    await expect(page.getByRole("row")).toHaveCount(7);
    const [download] = await Promise.all([
      page.waitForEvent("download"),
      page.getByRole("button", { name: /Add all dates to your calendar/ }).click(),
    ]);
    expect(download.suggestedFilename()).toBe("ai-regulation-deadlines.ics");
  });

  test("unknown routes show the branded 404", async ({ page }) => {
    const res = await page.goto("/does-not-exist");
    expect(res?.status()).toBe(404);
    await expect(page.getByRole("heading", { name: "This page could not be found." })).toBeVisible();
  });
});

test.describe("public site on a phone", () => {
  // iPhone 13 viewport and touch, on the default Chromium worker.
  const { defaultBrowserType: _browser, ...iphone } = devices["iPhone 13"];
  test.use(iphone);

  for (const path of PAGES) {
    test(`${path} has no horizontal scroll`, async ({ page }) => {
      await page.goto(path);
      const overflow = await page.evaluate(() => document.documentElement.scrollWidth - window.innerWidth);
      expect(overflow).toBeLessThanOrEqual(0);
    });
  }

  test("mobile menu opens, expands a section, and navigates", async ({ page }) => {
    await page.goto("/");
    await page.getByRole("button", { name: "Open menu" }).click();
    const dialog = page.getByRole("dialog", { name: "Menu" });
    await dialog.getByRole("button", { name: "Solutions" }).click();
    await dialog.getByRole("link", { name: /Compliance/ }).click();
    await expect(page).toHaveURL(/\/who-its-for#compliance$/);
    await expect(page.getByRole("dialog", { name: "Menu" })).toHaveCount(0);
  });
});
