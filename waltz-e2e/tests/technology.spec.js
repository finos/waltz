import { test, expect } from "@playwright/test";
import { authenticate, createApp, login, uniqueName } from "./helpers/api.js";
import { seedAppTechnology } from "./helpers/technology.js";

const baseURL = process.env.WALTZ_BASE_URL ?? "http://localhost:8080";

/**
 * Technology-area e2e (issue #7573, sub-task of #6071).
 *
 * Covers the checklist: show application-level technology, search for servers and
 * databases, confirm the aggregate roll-ups, and export through the aggregate pages.
 *
 * Each test seeds its OWN application and technology through the REST API (asset-inventory
 * bulk endpoints + usage-link endpoints — see helpers/technology.js), so the suite does not
 * depend on the LoadAll sample data. On backends where those write endpoints are not
 * deployed the seed is a no-op and the test skips.
 *
 * UI touch-points:
 *  - App Technology is a dynamic section (id 18) on /application/{id}?sections=18, a
 *    uib-tabset with Overview / Software / Servers / Databases / ... tabs, each detail
 *    tab a ui-grid.
 *  - The aggregate "Technologies" roll-up is a dynamic section (id 19) on any parent
 *    entity, e.g. /org-units/{id}?sections=19, rendering waltz-sub-section pie charts and
 *    "Export Servers" / "Export Databases" data-extract links.
 */

const ORG_UNIT_ID = 10;                       // createApp's default baseline org unit
const TECHNOLOGY_SECTION_ID = 18;
const TECHNOLOGY_SUMMARY_SECTION_ID = 19;

/** Seed a fresh application with one server and one database; skip if endpoints are absent. */
async function seedApp(token) {
    const suffix = uniqueName("t").replace(/[^a-z0-9]/gi, "");
    const app = await createApp(baseURL, token, `ts_tech_${suffix}`, ORG_UNIT_ID);
    const seeded = await seedAppTechnology(baseURL, token, app.id, suffix);
    return { app, seeded };
}

/** Open the global search overlay and run a query, returning the results region locator. */
async function globalSearch(page, query) {
    await page.locator(".navbar-right").getByTestId("search-button").click();
    const searchRegion = page.locator(".wnso-search-region");
    await searchRegion.locator("input[type=search]").fill(query);
    return page.locator(".wnso-search-results");
}


test("application technology section lists servers and databases", async ({ page, context }) => {
    const token = await login(baseURL);
    const { app, seeded } = await seedApp(token);

    test.skip(!seeded, "technology write endpoints not available on this backend");

    await authenticate(context, token);
    await page.goto(`/application/${app.id}?sections=${TECHNOLOGY_SECTION_ID}`);

    // The Technology section renders its uib-tabset (tabs are anchor "links"); the
    // Servers/Databases tabs only exist inside this section.
    const tabs = page.locator(".nav-tabs");
    const serversTab = tabs.getByRole("link", { name: "Servers", exact: true });
    await expect(serversTab).toBeVisible();

    // Servers tab: the detail grid (ng-if="showServerDetail") renders the seeded server.
    await serversTab.click();
    await expect(page.locator(".ui-grid-cell-contents").filter({ hasText: seeded.hostname }).first()).toBeVisible();

    // Databases tab: renders the seeded database.
    await tabs.getByRole("link", { name: "Databases", exact: true }).click();
    await expect(page.locator(".ui-grid-cell-contents").filter({ hasText: seeded.databaseName }).first()).toBeVisible();
});


test("global search finds an existing server and opens its landing page", async ({ page, context }) => {
    const token = await login(baseURL);
    const { seeded } = await seedApp(token);

    test.skip(!seeded, "technology write endpoints not available on this backend");

    const hostname = seeded.hostname;

    await authenticate(context, token);
    await page.goto("/");

    // Search by the seeded hostname; the SERVER search result's name IS the hostname.
    const results = await globalSearch(page, hostname);
    const result = results.getByTestId("entity-name").getByText(hostname, { exact: true });
    await expect(result.first()).toBeVisible();
    await result.first().click();

    // Landing page is the server view; its page header renders the hostname.
    const header = page
        .locator(".waltz-page-header")
        .getByTestId("header-name-truncated")
        .getByText(hostname, { exact: true });
    await expect(header.first()).toBeVisible();
});


test("global search finds an existing database and opens its landing page", async ({ page, context }) => {
    const token = await login(baseURL);
    const { seeded } = await seedApp(token);

    test.skip(!seeded, "technology write endpoints not available on this backend");

    const databaseName = seeded.databaseName;

    await authenticate(context, token);
    await page.goto("/");

    // Search by the seeded database name; the DATABASE search result's name IS that name.
    const results = await globalSearch(page, databaseName);
    const result = results.getByTestId("entity-name").getByText(databaseName, { exact: true });
    await expect(result.first()).toBeVisible();
    await result.first().click();

    // Landing page is the database view; its page header renders the database name.
    const header = page
        .locator(".waltz-page-header")
        .getByTestId("header-name-truncated")
        .getByText(databaseName, { exact: true });
    await expect(header.first()).toBeVisible();
});


test("aggregate technology roll-up shows server and database summaries", async ({ page, context }) => {
    const token = await login(baseURL);
    const { seeded } = await seedApp(token);

    test.skip(!seeded, "technology write endpoints not available on this backend");

    await authenticate(context, token);
    // The roll-up aggregates over the org unit's applications; the seeded app sits in ORG_UNIT_ID.
    await page.goto(`/org-units/${ORG_UNIT_ID}?sections=${TECHNOLOGY_SUMMARY_SECTION_ID}`);

    // Servers and Databases sub-sections roll up their pie charts (e.g. "By Operating System").
    await expect(page.locator("waltz-sub-section").filter({ hasText: "Servers" }).first()).toBeVisible();
    await expect(page.locator("waltz-sub-section").filter({ hasText: "Databases" }).first()).toBeVisible();
    await expect(page.getByText("By Operating System", { exact: true }).first()).toBeVisible();
});


test("export servers as CSV from the aggregate technology page", async ({ page, context }) => {
    const token = await login(baseURL);
    const { seeded } = await seedApp(token);

    test.skip(!seeded, "technology write endpoints not available on this backend");

    await authenticate(context, token);
    await page.goto(`/org-units/${ORG_UNIT_ID}?sections=${TECHNOLOGY_SUMMARY_SECTION_ID}`);

    // The "Export Servers" data-extract link is a dropdown (cloud-download toggle + caret).
    const exportToggle = page.locator(".waltz-sub-section-controls a").filter({ hasText: "Export Servers" });
    await expect(exportToggle.first()).toBeVisible();
    await exportToggle.first().click();

    // Choose "Export as csv" (each export link has its own body-appended menu, so target the
    // open/visible one) and confirm a download is produced with the server EOL columns.
    const downloadPromise = page.waitForEvent("download");
    await page
        .locator(".dropdown-menu a.clickable")
        .filter({ hasText: "Export as csv", visible: true })
        .click();
    const download = await downloadPromise;

    const path = await download.path();
    const fs = await import("node:fs/promises");
    const contents = await fs.readFile(path, "utf8");
    expect(contents).toContain("Host Name");
});
