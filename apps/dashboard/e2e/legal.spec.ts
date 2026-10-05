import { expect, test } from "@playwright/test";

const LEGAL_PAGES = ["/terms", "/privacy", "/dpa", "/refunds", "/disclaimer", "/security", "/subprocessors", "/msa", "/order-form"];

test.describe("legal pages", () => {
  for (const path of LEGAL_PAGES) {
    test(`${path} loads with one h1 and a last-updated date`, async ({ page }) => {
      const res = await page.goto(path);
      expect(res?.status()).toBe(200);
      await expect(page.locator("h1")).toHaveCount(1);
      await expect(page.getByText(/Last updated:/)).toBeVisible();
    });
  }

  for (const path of ["/terms", "/order-form", "/disclaimer", "/msa"]) {
    test(`${path} says it is not legal advice`, async ({ page }) => {
      await page.goto(path);
      await expect(page.locator("main")).toContainText(/not (provide )?legal advice/i);
    });
  }

  for (const path of ["/privacy", "/dpa", "/security"]) {
    test(`${path} links the subprocessors list`, async ({ page }) => {
      await page.goto(path);
      await expect(page.locator('main a[href="/subprocessors"]').first()).toBeVisible();
    });
  }

  test("terms and DPA incorporate the Common Paper standards by version", async ({ page }) => {
    await page.goto("/terms");
    await expect(page.locator('a[href="https://commonpaper.com/standards/cloud-service-agreement/2.1/"]').first()).toBeVisible();
    await page.goto("/dpa");
    await expect(page.locator('a[href="https://commonpaper.com/standards/data-processing-agreement/1.1/"]')).toBeVisible();
  });

  test("refund policy states the 14-day first-payment refund", async ({ page }) => {
    await page.goto("/refunds");
    await expect(page.locator("main")).toContainText("Refunds within 14 days of your first payment for any plan");
  });

  test("privacy policy has the automated-decisions statement", async ({ page }) => {
    await page.goto("/privacy");
    await expect(page.locator("main")).toContainText(
      "We do not make decisions that significantly affect individuals by automated means.",
    );
  });

  test("security.txt is served as plain text with a contact and expiry", async ({ request }) => {
    const res = await request.get("/.well-known/security.txt");
    expect(res.status()).toBe(200);
    expect(res.headers()["content-type"]).toContain("text/plain");
    const body = await res.text();
    expect(body).toMatch(/^Contact: mailto:\S+@\S+/m);
    expect(body).toMatch(/^Expires: \d{4}-\d{2}-\d{2}T/m);
    expect(body).toMatch(/^Policy: https?:\/\/\S+\/security$/m);
  });

  test("the home page loads no Google Tag Manager", async ({ request }) => {
    const html = await (await request.get("/")).text();
    expect(html).not.toContain("googletagmanager");
  });
});
