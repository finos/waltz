import { test, expect } from "@playwright/test";
import { authenticate, createApp, login, uniqueName } from "./helpers/api.js";

const baseURL = process.env.WALTZ_BASE_URL ?? "http://localhost:8080";

/**
 * Seed an application purely via the REST API, then drive the UI to search for it.
 * Mirrors the Java ApplicationSearchTest, but with API-based setup.
 */
test("global search finds an app created via the REST API", async ({ page, context }) => {
    const token = await login(baseURL);
    const appName = uniqueName("ts_search_app");
    await createApp(baseURL, token, appName);

    await authenticate(context, token);
    await page.goto("/");

    await page.locator(".navbar-right").getByTestId("search-button").click();
    const searchRegion = page.locator(".wnso-search-region");
    await searchRegion.locator("input[type=search]").fill(appName);

    const result = page
        .locator(".wnso-search-results")
        .getByTestId("entity-name")
        .getByText(appName);
    await expect(result).toBeVisible();
    await result.click();

    const header = page
        .locator(".waltz-page-header")
        .getByTestId("header-small")
        .getByText(appName);
    await expect(header).toBeVisible();
});
