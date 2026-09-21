import { test, expect } from "@playwright/test";
import { apiContext, authenticate, createApp, login, uniqueName } from "./helpers/api.js";

const baseURL = process.env.WALTZ_BASE_URL ?? "http://localhost:8080";

/**
 * Seed an app group (owned by the caller) containing the supplied applications, via the
 * REST API the UI itself uses (see AppGroupEndpoint.java / AppGroupHelper.java):
 *   1. POST /api/app-group                       -> createNewGroup (returns new group id, caller becomes OWNER)
 *   2. POST /api/app-group/id/:id                -> updateOverview (set name + PUBLIC kind)
 *   3. POST /api/app-group/id/:id/applications   -> addApplication (one per app)
 */
async function seedAppGroup(token, name, apps) {
    const ctx = await apiContext(baseURL, token);

    const createResp = await ctx.post("/api/app-group");
    if (!createResp.ok()) {
        throw new Error(`create app-group failed: ${createResp.status()} ${await createResp.text()}`);
    }
    const groupId = await createResp.json();

    const overviewResp = await ctx.post(`/api/app-group/id/${groupId}`, {
        data: { id: groupId, name, description: name, appGroupKind: "PUBLIC" }
    });
    if (!overviewResp.ok()) {
        throw new Error(`update app-group overview failed: ${overviewResp.status()} ${await overviewResp.text()}`);
    }

    for (const app of apps) {
        const addResp = await ctx.post(`/api/app-group/id/${groupId}/applications`, { data: app.id });
        if (!addResp.ok()) {
            throw new Error(`add application to group failed: ${addResp.status()} ${await addResp.text()}`);
        }
    }

    await ctx.dispose();
    return groupId;
}

/**
 * Open the "Report Grids" dynamic section from the sidebar (see Sidebar.svelte / dynamic-section
 * definitions — the section is registered for APP_GROUP as "Report Grids").
 */
async function openReportGridsSection(page) {
    const sectionButton = page
        .locator(".sidebar-expanded button, .sidebar-collapsed button")
        .filter({ hasText: "Report Grids" })
        .first();
    await sectionButton.click();
}

/**
 * Add a BASELINE fixed column: pick an entity kind from the selector dropdown, then click the
 * first available field/row in the resulting picker. Scoped to the add panel (.waltz-sticky-part)
 * so we never accidentally click the column-summary list on the left.
 */
async function addBaselineColumn(page, entityKind) {
    const panel = page.locator(".waltz-sticky-part");

    await panel.getByRole("button", { name: "Select an entity kind" }).click();
    await panel.getByText(entityKind, { exact: true }).click();

    const firstRow = panel.locator("tr.clickable").first();
    await expect(firstRow).toBeVisible();
    await firstRow.click();

    // reset the selector back to its initial state ready for the next column
    await panel.getByRole("button", { name: "Close" }).click();
}

/** Seed two applications in a fresh public app group; return the group id and the apps. */
async function seedGroupWithApps(token) {
    const app1 = await createApp(baseURL, token, uniqueName("rg_app1"));
    const app2 = await createApp(baseURL, token, uniqueName("rg_app2"));
    const groupId = await seedAppGroup(token, uniqueName("rg_group"), [app1, app2]);
    return { app1, app2, groupId };
}

/**
 * Create a report grid from the picker (title + description). Leaves the UI on the Column
 * Editor tab (where the app switches on create) and returns the grid name.
 */
async function createGrid(page) {
    await openReportGridsSection(page);
    await page.getByRole("button", { name: "Create a new report grid" }).click();

    const gridName = uniqueName("rg_grid");
    const form = page.locator("form");
    await expect(form.locator("#title")).toBeVisible();
    await form.locator("#title").fill(gridName);
    await form.locator("#description").fill(`description for ${gridName}`);
    await form.locator("button[type=submit]").click();

    // On create the UI switches to the Column Editor tab.
    await expect(page.locator("label[for=columns]")).toBeVisible();
    return gridName;
}

test("report grid: create, add columns, update overview, edit and remove columns, delete", async ({ page, context }) => {
    const token = await login(baseURL);

    // --- Seed: two apps in an app group (Scenario setup) ---
    const { app1, app2, groupId } = await seedGroupWithApps(token);

    await authenticate(context, token);
    await page.goto(`/app-group/${groupId}`);

    // ---------------------------------------------------------------------------------------
    // Scenario 1: create a report grid (title + description)
    // ---------------------------------------------------------------------------------------
    const gridName = await createGrid(page);

    // ---------------------------------------------------------------------------------------
    // Scenario 2: add a couple of BASELINE columns (Application field + Org Unit field).
    // No custom assessment definition is created (there is no create API for those).
    // ---------------------------------------------------------------------------------------
    await page.locator("label[for=columns]").click();

    await addBaselineColumn(page, "Application");
    await addBaselineColumn(page, "Org Unit");

    // Persist the column changes.
    await page.getByRole("button", { name: "Save this report" }).click();

    // ---------------------------------------------------------------------------------------
    // Scenario 3: verify the grid shows the seeded apps.
    // ---------------------------------------------------------------------------------------
    const grid = page.locator("waltz-grid-with-search");
    await expect(grid).toBeVisible();
    await expect(grid.getByText(app1.name, { exact: true }).first()).toBeVisible();
    await expect(grid.getByText(app2.name, { exact: true }).first()).toBeVisible();

    // ---------------------------------------------------------------------------------------
    // Scenario 4: update the grid's name and description (Overview tab -> Edit Grid Overview).
    // ---------------------------------------------------------------------------------------
    await page.locator("label[for=overview]").click();
    await page.getByRole("button", { name: "Edit Grid Overview" }).click();

    const newName = uniqueName("rg_grid_upd");
    await page.locator("#title").fill(newName);
    await page.locator("#description").fill(`updated description for ${newName}`);
    // The Overview editor's save button is a plain "Save" (distinct from the columns
    // editor's "Save this report"); it stays disabled until the name changes.
    await page.getByRole("button", { name: "Save", exact: true }).click();

    // Back in view mode; the Overview tab label renders "Overview - {name}".
    await expect(page.locator("label[for=overview]")).toContainText(newName);

    // ---------------------------------------------------------------------------------------
    // Scenario 5: edit a column -- override its display name (Column Editor tab).
    // Each summary row's action buttons: 4 titled position buttons, then an untitled
    // pencil (edit) and an untitled trash (remove); they reveal on row hover.
    // ---------------------------------------------------------------------------------------
    await page.locator("label[for=columns]").click();

    // The previous save reloads the grid; wait for the summary to settle at its two columns
    // before interacting, so we never act on / count a transient mid-reload state.
    const columnRows = page.locator("tr.waltz-visibility-parent");
    await expect(columnRows).toHaveCount(2);

    const firstColumn = columnRows.first();
    await firstColumn.hover();
    await firstColumn.locator("button.btn-skinny:not([title])").first().click();

    const overrideName = uniqueName("rg_col");
    const editPanel = page.locator(".waltz-sticky-part");
    await editPanel.locator("#displayName").fill(overrideName);
    await editPanel.locator("#displayName").blur();          // commits on the change event

    await page.getByRole("button", { name: "Save this report" }).click();

    // The reloaded summary lists the column under its overridden name.
    await expect(columnRows.filter({ hasText: overrideName })).toBeVisible();

    // ---------------------------------------------------------------------------------------
    // Scenario 6: remove a column (Column Editor tab).
    // ---------------------------------------------------------------------------------------
    // Re-open the editor and wait for the summary to settle after the previous save/reload,
    // so the removal acts on a stable two-column state.
    await page.locator("label[for=columns]").click();
    await expect(columnRows).toHaveCount(2);

    const lastColumn = columnRows.last();
    await lastColumn.hover();
    await lastColumn.locator("button.btn-skinny:not([title])").last().click();

    await page.locator(".waltz-sticky-part").getByRole("button", { name: "Remove", exact: true }).click();
    await page.getByRole("button", { name: "Save this report" }).click();

    await expect(columnRows).toHaveCount(1);

    // ---------------------------------------------------------------------------------------
    // Scenario 7: delete the grid.
    // ---------------------------------------------------------------------------------------
    await page.locator("label[for=overview]").click();

    await page.getByRole("button", { name: "Delete Grid" }).click();
    await page.getByRole("button", { name: "Yes, delete this grid" }).click();

    // Grid is gone: it no longer appears in the grid picker list.
    await expect(page.getByRole("cell", { name: newName })).toHaveCount(0);
});


test("report grid: add, render, and edit a derived (custom) column", async ({ page, context }) => {
    const token = await login(baseURL);

    const { groupId } = await seedGroupWithApps(token);

    await authenticate(context, token);
    await page.goto(`/app-group/${groupId}`);

    await createGrid(page);

    // ---------------------------------------------------------------------------------------
    // Scenario 1: add a derived column. Unlike a fixed column it has no entity field; its value
    // comes from a JEXL derivation script (see docs/features/report-grids). mkResult(...) yields
    // one cell per subject, so the constant below renders for every application in the group.
    // ---------------------------------------------------------------------------------------
    await page.locator("label[for=columns]").click();

    const panel = page.locator(".waltz-sticky-part");
    await panel.getByRole("button", { name: "add a derived column" }).click();

    const derivedName = uniqueName("rg_derived");
    const derivedExtId = uniqueName("rgd").replace(/[^a-z0-9]/gi, "");
    await panel.locator("#title").fill(derivedName);                 // add form: #title is the display name
    await panel.locator("#externalId").fill(derivedExtId);
    await panel.locator("#derivationScript").fill('mkResult("Hello World")');
    await panel.getByRole("button", { name: "Done", exact: true }).click();

    // The column appears in the summary as "displayName (externalId)" the moment it is added.
    const columnRows = page.locator("tr.waltz-visibility-parent");
    await expect(columnRows.filter({ hasText: `${derivedName} (${derivedExtId})` })).toBeVisible();

    // Persist and confirm the script evaluates: the value renders once per seeded application.
    await page.getByRole("button", { name: "Save this report" }).click();
    const grid = page.locator("waltz-grid-with-search");
    await expect(grid.getByText("Hello World").first()).toBeVisible();

    // ---------------------------------------------------------------------------------------
    // Scenario 2: edit the derived column -- change its script and confirm the new value renders.
    // ---------------------------------------------------------------------------------------
    await page.locator("label[for=columns]").click();

    const derivedRow = columnRows.filter({ hasText: derivedName }).first();
    await derivedRow.hover();
    await derivedRow.locator("button.btn-skinny:not([title])").first().click();   // pencil (edit)

    await panel.locator("#derivationScript").fill('mkResult("Goodbye")');
    await panel.locator("#derivationScript").blur();                // commits on the change event

    await page.getByRole("button", { name: "Save this report" }).click();
    await expect(grid.getByText("Goodbye").first()).toBeVisible();
    await expect(grid.getByText("Hello World")).toHaveCount(0);
});
