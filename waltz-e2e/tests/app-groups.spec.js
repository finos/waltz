import { test, expect } from "@playwright/test";
import { apiContext, authenticate, createApp, login, uniqueName } from "./helpers/api.js";
import {
    createAppGroup,
    addAppToGroup,
    addOwner,
    getGroupDetail,
    removeOwner
} from "./helpers/app-groups.js";

const baseURL = process.env.WALTZ_BASE_URL ?? "http://localhost:8080";

/** First non-retired change initiative (baseline data — CIs have no create endpoint). */
async function pickChangeInitiative(token) {
    const ctx = await apiContext(baseURL, token);
    const cis = await (await ctx.get("/api/change-initiative/all")).json();
    await ctx.dispose();
    const ci = cis.find(c => c.organisationalUnitId);
    if (!ci) throw new Error("no change initiative with an org unit found");
    return ci;
}

/** Look up an org unit by id (GET /api/org-unit/:id). */
async function getOrgUnit(token, id) {
    const ctx = await apiContext(baseURL, token);
    const ou = await (await ctx.get(`/api/org-unit/${id}`)).json();
    await ctx.dispose();
    return ou;
}

/** Ids resolved by an APP_GROUP selector (the same selectors the group view page uses). */
async function selectorIds(token, path, groupId) {
    const ctx = await apiContext(baseURL, token);
    const resp = await ctx.post(path, {
        data: { entityReference: { kind: "APP_GROUP", id: groupId }, scope: "EXACT" }
    });
    const rows = await resp.json();
    await ctx.dispose();
    return rows.map(r => r.id);
}

/**
 * App Groups e2e (issue #6071). Groups are seeded via the REST API (the same endpoints the
 * UI uses — see AppGroupEndpoint.java) then driven / verified through the UI.
 * The admin user is auto-registered as OWNER when a group is created.
 */

// 1. Create a group with a name + description via API, verify it renders in the UI.
test("group created via API renders its name and description", async ({ page, context }) => {
    const token = await login(baseURL);
    const name = uniqueName("ts_ag_render");
    const description = "Description for " + name;
    const group = await createAppGroup(baseURL, token, { name, description, kind: "PUBLIC" });

    await authenticate(context, token);
    await page.goto(`/app-group/${group.id}`);

    await expect(
        page.locator(".waltz-page-header").getByTestId("header-name-truncated").getByText(name)
    ).toBeVisible();

    const overview = page.locator("waltz-app-group-overview");
    await expect(overview.getByText(description)).toBeVisible();
});

// 2. Toggle the group public/private via the edit UI and verify it persists.
test("public/private toggle persists after edit", async ({ page, context }) => {
    const token = await login(baseURL);
    const name = uniqueName("ts_ag_toggle");
    const group = await createAppGroup(baseURL, token, { name, description: "toggle test", kind: "PRIVATE" });

    await authenticate(context, token);
    await page.goto(`/app-group/${group.id}/edit`);

    const publicRadio = page.locator('input[type=radio][value="PUBLIC"]');
    await publicRadio.check();
    await page.locator("form button", { hasText: "Update" }).click();
    await expect(page.getByText("Group details updated")).toBeVisible();

    // API confirms the change persisted server-side...
    await expect
        .poll(async () => (await getGroupDetail(baseURL, token, group.id)).appGroup.appGroupKind)
        .toBe("PUBLIC");

    // ...and it survives a reload of the view page (header 'small' shows the kind).
    await page.goto(`/app-group/${group.id}`);
    await expect(
        page.locator(".waltz-page-header").getByTestId("header-small").getByText("PUBLIC")
    ).toBeVisible();
});

// 3. Add and then remove an application from the group.
// The app is added via the REST API (seed-via-API is the repo convention); the removal is
// exercised through the UI. Both add and remove are verified in the rendered edit page.
test("add then remove an application from the group", async ({ page, context }) => {
    const token = await login(baseURL);
    const groupName = uniqueName("ts_ag_apps");
    const appName = uniqueName("ts_ag_app");
    const group = await createAppGroup(baseURL, token, { name: groupName, description: "apps test", kind: "PUBLIC" });
    const app = await createApp(baseURL, token, appName);
    await addAppToGroup(baseURL, token, group.id, app.id);

    await authenticate(context, token);
    await page.goto(`/app-group/${group.id}/edit`);

    const appsSection = page.locator("waltz-section").filter({ hasText: "Applications In Group" });

    // The seeded app is rendered in the group's applications table.
    const appRow = appsSection.locator("tr").filter({ hasText: appName });
    await expect(appRow).toBeVisible();

    // Remove it via the UI and confirm the row disappears.
    await appRow.getByText("Remove").click();
    await expect(appsSection.locator("tr").filter({ hasText: appName })).toHaveCount(0);
});

// 4. Subscribe / unsubscribe from a group via the UI.
// Setup: demote admin from owner -> subscriber (owners get NOT_APPLICABLE, no buttons).
test("subscribe and unsubscribe toggles the buttons", async ({ page, context }) => {
    const token = await login(baseURL);
    const name = uniqueName("ts_ag_sub");
    const group = await createAppGroup(baseURL, token, { name, description: "sub test", kind: "PUBLIC" });
    // removeOwner re-registers admin as a plain subscriber, so the buttons become active.
    await removeOwner(baseURL, token, group.id, "admin");

    await authenticate(context, token);
    await page.goto(`/app-group/${group.id}`);

    const overview = page.locator("waltz-app-group-overview");
    const unsubscribeBtn = overview.getByRole("button", { name: "Unsubscribe" });
    const subscribeBtn = overview.getByRole("button", { name: "Subscribe" });

    // Starts subscribed (admin is a member/viewer).
    await expect(unsubscribeBtn).toBeVisible();
    await unsubscribeBtn.click();
    await expect(subscribeBtn).toBeVisible();

    await subscribeBtn.click();
    await expect(unsubscribeBtn).toBeVisible();
});

// 5. Verify a public group is searchable via global search.
test("public group is findable via global search", async ({ page, context }) => {
    const token = await login(baseURL);
    const name = uniqueName("ts_ag_search");
    const group = await createAppGroup(baseURL, token, { name, description: "search test", kind: "PUBLIC" });

    await authenticate(context, token);
    await page.goto("/");

    await page.locator(".navbar-right").getByTestId("search-button").click();
    const searchRegion = page.locator(".wnso-search-region");
    await searchRegion.locator("input[type=search]").fill(name);

    const result = page
        .locator(".wnso-search-results")
        .getByTestId("entity-name")
        .getByText(name);
    await expect(result).toBeVisible();
    await result.click();

    await expect(
        page.locator(".waltz-page-header").getByTestId("header-name-truncated").getByText(name)
    ).toBeVisible();
    expect(group.kind).toBe("PUBLIC");
});

// 6. Update the group's name and description via the edit form.
test("update the group name and description via the edit UI", async ({ page, context }) => {
    const token = await login(baseURL);
    const group = await createAppGroup(baseURL, token, { name: uniqueName("ts_ag_upd"), description: "before", kind: "PUBLIC" });

    await authenticate(context, token);
    await page.goto(`/app-group/${group.id}/edit`);

    // The overview form's name/description fields carry no id/testid; locate structurally.
    const overviewForm = page.locator("form", { has: page.locator('input[placeholder="Name of group"]') });
    const newName = uniqueName("ts_ag_upd_new");
    const newDescription = "updated description text";
    await overviewForm.locator('input[placeholder="Name of group"]').fill(newName);
    await overviewForm.locator("textarea").fill(newDescription);
    await overviewForm.getByRole("button", { name: "Update" }).click();
    await expect(page.getByText("Group details updated")).toBeVisible();

    // Persisted: the view page renders the new name and description.
    await page.goto(`/app-group/${group.id}`);
    await expect(
        page.locator(".waltz-page-header").getByTestId("header-name-truncated").getByText(newName)
    ).toBeVisible();
    await expect(page.locator("waltz-app-group-overview").getByText(newDescription)).toBeVisible();
});

// 7. Add then remove a change initiative through the UI (CIs are baseline; no create endpoint).
test("add and remove a change initiative via the UI", async ({ page, context }) => {
    const token = await login(baseURL);
    const group = await createAppGroup(baseURL, token, { name: uniqueName("ts_ag_ci"), description: "ci test", kind: "PUBLIC" });
    const ci = await pickChangeInitiative(token);

    await authenticate(context, token);
    await page.goto(`/app-group/${group.id}/edit`);

    const ciSection = page.locator("waltz-section").filter({ hasText: "Associated Change Initiatives" });

    // Add via the Single Editor's change-initiative ui-select (dropdown appended to <body>).
    await ciSection.locator(".ui-select-match").click();
    const search = page.locator("input.ui-select-search:visible");
    await search.fill(ci.externalId);
    const option = page.locator(".ui-select-choices-row").filter({ hasText: ci.externalId }).first();
    await expect(option).toBeVisible();
    await option.click();

    // The CI now appears in the group's change-initiatives table (Id column shows externalId).
    const ciRow = ciSection.locator("tr").filter({ hasText: ci.externalId });
    await expect(ciRow).toBeVisible();

    // Remove it via the UI and confirm it disappears.
    await ciRow.getByText("Remove").click();
    await expect(ciSection.locator("tr").filter({ hasText: ci.externalId })).toHaveCount(0);
});

// 8. Add an org unit; confirm its applications and change initiatives cascade into the group; remove it.
test("add and remove an org unit, cascading its applications and change initiatives", async ({ page, context }) => {
    const token = await login(baseURL);
    const group = await createAppGroup(baseURL, token, { name: uniqueName("ts_ag_ou"), description: "ou test", kind: "PUBLIC" });

    // Seed deterministic cascade data: a baseline CI in some org unit, plus our own app in that unit.
    const ci = await pickChangeInitiative(token);
    const orgUnit = await getOrgUnit(token, ci.organisationalUnitId);
    const app = await createApp(baseURL, token, uniqueName("ts_ag_ou_app"), orgUnit.id);

    await authenticate(context, token);
    await page.goto(`/app-group/${group.id}/edit`);

    const ouSection = page.locator("waltz-section").filter({ hasText: "Organisational Units In Group" });

    // Add the org unit via the entity-selector ui-select.
    await ouSection.locator(".ui-select-match").click();
    const search = page.locator("input.ui-select-search:visible");
    await search.fill(orgUnit.name);
    const option = page.locator(".ui-select-choices-row").filter({ hasText: orgUnit.name }).first();
    await expect(option).toBeVisible();
    await option.click();

    // The org unit appears in the group's org-units table.
    await expect(ouSection.locator("tr").filter({ hasText: orgUnit.name })).toBeVisible();

    // Cascade: the group's APP_GROUP selectors (which drive the view page) now resolve the org
    // unit's applications and change initiatives, not just directly-added entries.
    await expect
        .poll(async () => await selectorIds(token, "/api/app/selector", group.id))
        .toContain(app.id);
    await expect
        .poll(async () => await selectorIds(token, "/api/change-initiative/selector", group.id))
        .toContain(ci.id);

    // Remove the org unit via the UI and confirm it disappears.
    await ouSection.locator("tr").filter({ hasText: orgUnit.name }).getByText("Remove").click();
    await expect(ouSection.locator("tr").filter({ hasText: orgUnit.name })).toHaveCount(0);
});

// 9. Share the group by granting ownership: promote a subscriber to owner in the Group Users section.
test("share the group by promoting a subscriber to owner", async ({ page, context }) => {
    const token = await login(baseURL);
    const group = await createAppGroup(baseURL, token, { name: uniqueName("ts_ag_share"), description: "share test", kind: "PUBLIC" });

    // Seed a second user as a subscriber: add as owner, then remove (which re-registers them as a viewer).
    const member = uniqueName("e2e_share_user");
    await addOwner(baseURL, token, group.id, member);
    await removeOwner(baseURL, token, group.id, member);

    await authenticate(context, token);
    await page.goto(`/app-group/${group.id}/edit`);

    const usersSection = page.locator("waltz-section").filter({ hasText: "Group Users" });
    const memberRow = usersSection.locator("tr").filter({ hasText: member });
    await expect(memberRow).toBeVisible();

    // Promote the subscriber to owner (sharing ownership of the group).
    await memberRow.getByText("Promote to owner").click();
    await expect(page.getByText(`User: ${member} is now an owner of the group`)).toBeVisible();

    // The member is now an owner: their row offers the demote action.
    await expect(
        usersSection.locator("tr").filter({ hasText: member }).getByText("Demote to subscribers")
    ).toBeVisible();
});
