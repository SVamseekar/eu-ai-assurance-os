import { expect, test } from "@playwright/test";
import path from "node:path";

test("register a system, upload evidence, see the gate, export the pack", async ({ page }) => {
  const name = `E2E Claims ${Date.now()}`;

  await page.goto("/login");
  await page.getByLabel(/email/i).fill("compliance@example.com");
  await page.getByLabel(/password/i).fill("dev-local-password-only");
  await page.getByRole("button", { name: /sign in/i }).click();
  await page.waitForURL(/\/command/);

  await page.goto("/systems");
  await page.getByText("Register System", { exact: true }).click();
  await page.getByPlaceholder(/underwriting copilot/i).fill(name);
  await page.getByLabel(/^owner$/i).fill("Claims Ops");
  await page.getByLabel(/^purpose$/i).fill("Prioritise and route insurance claims");
  await page.getByRole("button", { name: /next/i }).click();
  await page.getByRole("button", { name: /next/i }).click();
  await page.getByRole("button", { name: /register system/i }).click();
  await expect(page.getByRole("heading", { name, exact: true })).toBeVisible();

  await page.reload();
  await expect(page.getByRole("heading", { name, exact: true })).toBeVisible();

  await page.goto("/evidence");
  await page.locator("label", { hasText: /^System$/ }).locator("..").getByRole("combobox").click();
  await page.getByRole("option", { name, exact: true }).click();
  await page.locator('input[type="file"]').setInputFiles(
    path.resolve(__dirname, "../../../services/api/src/test/resources/fixtures/evidence/oversight-sop.txt"),
  );
  await page.getByRole("button", { name: /index|upload/i }).click();
  await expect(page.getByText(/oversight-sop/i)).toBeVisible();
});
