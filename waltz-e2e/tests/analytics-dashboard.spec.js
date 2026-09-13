import { test, expect } from "@playwright/test";
import { authenticate, login } from "./helpers/api.js";

const baseURL = process.env.WALTZ_BASE_URL ?? "http://localhost:8080";

/**
 * Analytics Dashboard e2e (issue #7403).
 *
 * The dashboard (System Admin -> Analytics Dashboard) renders access-log and change-log
 * analytics over a user-selected date range + frequency. It reads existing access_log /
 * change_log data (populated by LoadAll and by normal usage), so these tests assert on the
 * page structure and successful chart loading rather than on specific data values - keeping
 * them robust across backends and seed states.
 *
 * UI touch-points (waltz-ng/client/system/svelte/analytics-dashboard):
 *  - route /system/analytics-dashboard (state main.system.analytics-dashboard)
 *  - filter controls: #startDate, #endDate, #grouping ("Auto" plus the `period` enum options)
 *  - section headings: "Access Log Analytics", "Change Log Analytics"
 *  - each chart is a waltz SubSection (.sub-section); load failures surface as a .alert-danger.
 */

const DASHBOARD_URL = "/system/analytics-dashboard";

async function openDashboard(page, context) {
    const token = await login(baseURL);
    await authenticate(context, token);
    await page.goto(DASHBOARD_URL);
    // Page header ("Analytics Dashboard" also appears in the breadcrumb, so match the first).
    await expect(page.getByRole("heading", { name: "Analytics Dashboard" }).first()).toBeVisible();
}


test("analytics dashboard renders filters and both analytics sections", async ({ page, context }) => {
    await openDashboard(page, context);

    await expect(page.locator("#startDate")).toBeVisible();
    await expect(page.locator("#endDate")).toBeVisible();
    await expect(page.locator("#grouping")).toBeVisible();

    // Grouping options: "Auto" plus the `period` enum (Day/Week/Month/Year).
    await expect(page.locator("#grouping option")).toHaveCount(5);

    await expect(page.getByRole("heading", { name: "Access Log Analytics" })).toBeVisible();
    await expect(page.getByRole("heading", { name: "Change Log Analytics" })).toBeVisible();
});


test("charts load without error for the default range", async ({ page, context }) => {
    await openDashboard(page, context);

    // Charts render inside waltz SubSections; wait for the first, then for loads to settle.
    await expect(page.locator(".sub-section").first()).toBeVisible();
    await page.waitForLoadState("networkidle");

    // No chart surfaced a load error (empty charts render an info NoData panel, not an error).
    await expect(page.locator(".alert-danger")).toHaveCount(0);
});


test("changing grouping re-renders the charts without error", async ({ page, context }) => {
    await openDashboard(page, context);
    await expect(page.locator(".sub-section").first()).toBeVisible();

    // Switch to weekly buckets; the charts reload off the derived key.
    await page.locator("#grouping").selectOption("WEEK");
    await expect(page.locator("#grouping")).toHaveValue("WEEK");

    await page.waitForLoadState("networkidle");
    await expect(page.locator(".alert-danger")).toHaveCount(0);
});
