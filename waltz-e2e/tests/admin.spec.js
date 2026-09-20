import { test, expect } from "@playwright/test";
import { authenticate, login, uniqueName, createApp } from "./helpers/api.js";
import {
    createUser, updateRoles, getUser, getRoles, loginAs,
    getRatingSchemes, getAssessmentDefinitions,
    createActor, getActors, deleteActor,
    getEndUserApps, createStaticPanel
} from "./helpers/admin.js";

const baseURL = process.env.WALTZ_BASE_URL ?? "http://localhost:8080";

/**
 * Admin area: users, roles and actors.
 * Setup is seeded via the REST API (createUser / updateRoles / loginAs); the scenarios
 * themselves are driven through the UI. Sample-data names are never hard-coded — users,
 * roles and actors are created with uniqueName(), and system roles are queried via API.
 */

/**
 * Scenario 1 — Create a user through the Manage Users UI.
 * /system/list -> Manage Users -> add a new user -> fill form -> register.
 */
test("create a user via the Manage Users UI", async ({ page, context }) => {
    const token = await login(baseURL);
    await authenticate(context, token);

    await page.goto("/system/list");
    await page.getByTestId("Manage Users").locator("a").click();

    await page.getByTestId("add-user-btn").click();

    const submit = page.getByTestId("submit-new-user-btn");
    await expect(submit).toBeDisabled();

    const username = uniqueName("e2e_user");
    await page.fill("#username", username);
    await page.fill("#password", uniqueName("pw"));
    await expect(submit).toBeEnabled();
    await submit.click();

    // After registration the UI switches to the user detail view (h4 = username).
    await expect(page.getByRole("heading", { name: username })).toBeVisible();

    // And the user is persisted.
    const created = await getUser(baseURL, token, username);
    expect(created.userName).toBe(username);
});

/**
 * Scenario 2 — Grant a userSelectable role to a user via the UI, verify it, then remove it.
 * The user is seeded via API; the role is chosen dynamically from GET /api/role.
 */
test("grant then remove a role for a user via the UI", async ({ page, context }) => {
    const token = await login(baseURL);
    const username = uniqueName("e2e_role_user");
    await createUser(baseURL, token, username, uniqueName("pw"));

    const roles = await getRoles(baseURL, token);
    const role = roles.find(r => r.userSelectable && !r.isCustom);
    expect(role, "expected at least one userSelectable system role").toBeTruthy();

    await authenticate(context, token);

    const openUser = async () => {
        await page.goto("/user/management");
        await page.getByPlaceholder("Search for a user...").fill(username);
        await page.getByRole("button", { name: username }).click();
        await expect(page.getByRole("heading", { name: username })).toBeVisible();
        await page.getByPlaceholder("Search for a role...").fill(role.name);
        return page.locator("tbody tr").filter({
            has: page.getByText(role.key, { exact: true })
        });
    };

    // --- grant
    let row = await openUser();
    await row.getByRole("checkbox").first().check();
    await page.fill("#comment", "granting via e2e");
    await page.getByRole("button", { name: "Save Updates" }).click();
    await expect(page.getByText(/Successfully updated roles/)).toBeVisible();

    const afterGrant = await getUser(baseURL, token, username);
    expect(afterGrant.roles).toContain(role.key);

    // --- remove (fresh page load so the user list reflects the granted role)
    row = await openUser();
    await expect(row.getByRole("checkbox").first()).toBeChecked();
    await row.getByRole("checkbox").first().uncheck();
    await page.fill("#comment", "removing via e2e");
    await page.getByRole("button", { name: "Save Updates" }).click();
    await expect(page.getByText(/Successfully updated roles/)).toBeVisible();

    const afterRemove = await getUser(baseURL, token, username);
    expect(afterRemove.roles).not.toContain(role.key);
});

/**
 * Scenario 3 — Create a custom role via the Roles UI (/role/list -> Create Custom Role).
 */
test("create a custom role via the UI", async ({ page, context }) => {
    const token = await login(baseURL);
    await authenticate(context, token);

    await page.goto("/role/list");

    const roleName = uniqueName("e2e Custom Role");
    await page.getByPlaceholder("Role Name").fill(roleName);
    await page.getByPlaceholder("Role Description").fill("created by e2e");
    await page.getByRole("button", { name: "Save Changes" }).click();

    await expect(page.getByText(/Role created successfully/)).toBeVisible();

    // The new custom role is persisted and now returned by the API.
    const roles = await getRoles(baseURL, token);
    const created = roles.find(r => r.name === roleName);
    expect(created, "custom role should exist via API").toBeTruthy();
    expect(created.isCustom).toBe(true);

    // And it shows up in the roles table.
    await page.getByPlaceholder("Search for a role...").fill(roleName);
    await expect(page.getByRole("link", { name: roleName })).toBeVisible();
});

/**
 * Scenario 4 — Create then update an actor via the System Admin > Actors UI.
 * Actor create/update requires the ACTOR_ADMIN role (admin only has ADMIN), so a dedicated
 * user is seeded and granted ACTOR_ADMIN, and the UI is driven as that user.
 */
test("create then update an actor via the UI", async ({ page, context }) => {
    const adminToken = await login(baseURL);
    const actorAdmin = uniqueName("e2e_actor_admin");
    const password = uniqueName("pw");
    await createUser(baseURL, adminToken, actorAdmin, password);
    await updateRoles(baseURL, adminToken, actorAdmin, ["ACTOR_ADMIN"], "e2e actor admin");

    const userToken = await loginAs(baseURL, actorAdmin, password);
    await authenticate(context, userToken);

    await page.goto("/system/actors");

    // --- create
    await page.getByRole("button", { name: "Add", exact: true }).click();
    const actorName = uniqueName("e2e_actor");
    await page.fill("#wav-name", actorName);
    await page.fill("#wav-desc", "created by e2e");
    await page.getByRole("button", { name: "Save", exact: true }).click();

    await expect(page.getByText("Created", { exact: true })).toBeVisible();

    // The new actor appears in the list.
    await page.getByPlaceholder("Filter...").fill(actorName);
    const actorRow = page.locator("tbody tr").filter({ hasText: actorName });
    await expect(actorRow).toBeVisible();

    // --- update: select the actor and edit its description via the editable field.
    await actorRow.click();

    // The editable field's edit / save controls render as icons only (no text label).
    const descField = page.locator("waltz-editable-field").nth(1);
    await descField.locator("a").click();
    await descField.locator("input[type=text]").fill("updated by e2e");
    await descField.locator("button.btn-success").click();

    await expect(page.getByText("Updated", { exact: true })).toBeVisible();
});

/**
 * Scenario 5 — Remove an actor.
 * The actors admin screen intentionally offers no delete control (an actor may be referenced
 * by physical flows), so removal is exercised through the REST endpoint the store exposes:
 * DELETE /api/actor/:id. Both create and delete require the ACTOR_ADMIN role.
 */
test("remove an actor via the REST API", async ({ context }) => {
    const adminToken = await login(baseURL);
    const actorAdmin = uniqueName("e2e_actor_rm");
    const password = uniqueName("pw");
    await createUser(baseURL, adminToken, actorAdmin, password);
    await updateRoles(baseURL, adminToken, actorAdmin, ["ACTOR_ADMIN"], "e2e actor remove");
    const token = await loginAs(baseURL, actorAdmin, password);
    await authenticate(context, token);

    const name = uniqueName("e2e_actor_rm");
    const id = await createActor(baseURL, token, name, "to be removed by e2e");
    expect((await getActors(baseURL, token)).some(a => a.id === id)).toBe(true);

    const removed = await deleteActor(baseURL, token, id);
    expect(removed).toBe(true);

    expect((await getActors(baseURL, token)).some(a => a.id === id)).toBe(false);
});

/**
 * Scenario 6 — Create, update and delete a rating scheme via the Rating Schemes UI.
 * /system/rating-schemes -> add -> edit scheme -> remove. A freshly created scheme has no
 * usages, so the (usage-guarded) Remove control is enabled.
 */
test("create, update and delete a rating scheme via the UI", async ({ page, context }) => {
    const token = await login(baseURL);
    await authenticate(context, token);

    await page.goto("/system/rating-schemes");

    // --- create
    await page.getByRole("button", { name: "Add new rating scheme" }).click();
    const schemeName = uniqueName("e2e Rating Scheme");
    await page.fill("#name", schemeName);
    await page.fill("#description", "created by e2e");
    await page.getByRole("button", { name: "Save", exact: true }).click();
    await expect(page.getByText("Successfully saved rating scheme").first()).toBeVisible();

    let schemes = await getRatingSchemes(baseURL, token);
    expect(schemes.find(s => s.name === schemeName), "scheme should exist via API").toBeTruthy();

    // --- update the description via Edit Scheme
    let row = page.locator("tr").filter({ hasText: schemeName });
    await row.locator("button").filter({ hasText: "Edit Scheme" }).click();
    await page.fill("#description", "updated by e2e");
    await page.getByRole("button", { name: "Save", exact: true }).click();
    await expect(page.getByText("Successfully saved rating scheme").first()).toBeVisible();

    schemes = await getRatingSchemes(baseURL, token);
    expect(schemes.find(s => s.name === schemeName).description).toBe("updated by e2e");

    // --- delete
    row = page.locator("tr").filter({ hasText: schemeName });
    await row.getByRole("button", { name: "Remove" }).click();
    await expect(page.getByText("Confirm rating scheme removal")).toBeVisible();
    await page.locator("button.btn-danger").filter({ hasText: "Remove" }).click();
    await expect(page.getByText("Successfully removed rating scheme")).toBeVisible();

    schemes = await getRatingSchemes(baseURL, token);
    expect(schemes.find(s => s.name === schemeName), "scheme should be gone").toBeFalsy();
});

/**
 * Scenario 7 — Create, update and delete an assessment definition via the UI.
 * /system/assessment-definitions. A rating scheme id is required, so one is read from the API
 * and selected in the form. entityKind APPLICATION carries no qualifier.
 */
test("create, update and delete an assessment definition via the UI", async ({ page, context }) => {
    const token = await login(baseURL);
    const schemes = await getRatingSchemes(baseURL, token);
    expect(schemes.length, "expected at least one rating scheme").toBeGreaterThan(0);
    const scheme = schemes[0];
    await authenticate(context, token);

    await page.goto("/system/assessment-definitions");

    // --- create
    await page.getByRole("button", { name: "Add new assessment definition" }).click();
    const defName = uniqueName("e2e Assessment Def");
    await page.fill("#name", defName);
    await page.selectOption("#ratingScheme", String(scheme.id));
    await page.selectOption("#entityKind", "APPLICATION");
    await page.fill("#description", "created by e2e");
    await page.getByRole("button", { name: "Save", exact: true }).click();
    await expect(page.getByText("Successfully saved assessment definition").first()).toBeVisible();

    let defs = await getAssessmentDefinitions(baseURL, token);
    expect(defs.find(d => d.name === defName), "definition should exist via API").toBeTruthy();

    // --- update the description
    let row = page.locator("tr").filter({ hasText: defName });
    await row.getByRole("button", { name: "Edit" }).click();
    await page.fill("#description", "updated by e2e");
    await page.getByRole("button", { name: "Save", exact: true }).click();
    await expect(page.getByText("Successfully saved assessment definition").first()).toBeVisible();

    defs = await getAssessmentDefinitions(baseURL, token);
    expect(defs.find(d => d.name === defName).description).toBe("updated by e2e");

    // --- delete (no ratings exist, so removal is allowed immediately)
    row = page.locator("tr").filter({ hasText: defName });
    await row.getByRole("button", { name: "Delete" }).click();
    await expect(page.getByText("Confirm assessment definition removal")).toBeVisible();
    await page.getByRole("button", { name: "Remove" }).click();
    await expect(page.getByText("Successfully removed assessment definition")).toBeVisible();

    defs = await getAssessmentDefinitions(baseURL, token);
    expect(defs.find(d => d.name === defName), "definition should be gone").toBeFalsy();
});

/**
 * Scenario 8 — Rebuild a hierarchy via the Hierarchy Maintenance UI.
 * /system/hierarchies -> "Rebuild for <kind>" -> success toast reporting the record count.
 */
test("rebuild a hierarchy via the UI", async ({ page, context }) => {
    const token = await login(baseURL);
    await authenticate(context, token);

    await page.goto("/system/hierarchies");
    // The rebuild controls are clickable anchors without an href (so no link role).
    await page.locator("a.clickable").filter({ hasText: "Rebuild for" }).first().click();

    await expect(page.getByText(/Hierarchy rebuilt for .+ with \d+ records/)).toBeVisible();
});

/**
 * Scenario 9 — The Orphans admin view lists the orphan categories and cleanup actions.
 * Sample data is internally consistent, so the categories render with zero counts; the test
 * asserts the view (categories + cleanup actions) renders rather than any orphan rows.
 */
test("the orphans admin view lists categories and cleanup actions", async ({ page, context }) => {
    const token = await login(baseURL);
    await authenticate(context, token);

    await page.goto("/system/orphans");

    await expect(page.getByText("Applications referencing non-existent Org Units")).toBeVisible();
    await expect(page.getByText("Logical Flows referencing non-existent applications")).toBeVisible();
    await expect(page.getByRole("button", { name: "Cleanup Logical Flows" })).toBeVisible();
    await expect(page.getByRole("button", { name: "Cleanup Physical Flows" })).toBeVisible();
});

/**
 * Scenario 10 — Reassign recipients via the Reassign Recipients UI.
 * /system/reassign-recipients. Under sample data the recipients already match involvements, so
 * the reassignment reports zero changes; the test asserts the action succeeds (toast).
 */
test("reassign recipients via the admin UI", async ({ page, context }) => {
    const token = await login(baseURL);
    await authenticate(context, token);

    await page.goto("/system/reassign-recipients");
    await expect(page.getByRole("heading", { name: "Attestations" })).toBeVisible();

    // Two action buttons read exactly "Reassign recipients" (Attestations then Surveys); the exact,
    // case-sensitive match avoids the page-header's collapse button (" Reassign Recipients"). The
    // Surveys button (second) is used: the attestation reassignment errors on an empty attestation
    // set under sample data, whereas survey reassignment succeeds (reporting zero changes).
    await page.getByRole("button", { name: "Reassign recipients", exact: true }).nth(1).click();
    await expect(page.getByText(/Successfully reassigned recipients/)).toBeVisible();
});

/**
 * Scenario 11 — Promote a EUDA to a full application via the EUDA Promotion UI.
 * /system/euda-list. EUDAs are feed/sample-data loaded (no create endpoint), so a not-yet
 * promoted one is discovered via the API; the test is skipped if none exist.
 */
test("promote a EUDA to an application via the UI", async ({ page, context }, testInfo) => {
    const token = await login(baseURL);
    const unpromoted = (await getEndUserApps(baseURL, token)).filter(e => !e.isPromoted);
    test.skip(unpromoted.length === 0, "no un-promoted EUDA available under sample data");
    // Pick by parallel slot so concurrent runs (e.g. --repeat-each) don't race the same EUDA.
    const euda = unpromoted[testInfo.parallelIndex % unpromoted.length];

    await authenticate(context, token);

    await page.goto("/system/euda-list");

    const row = page.locator("tr.clickable").filter({ hasText: euda.name }).first();
    await row.click();

    // The first Promote (skinny) opens the confirmation; the success button confirms.
    await page.locator("button.btn-skinny").filter({ hasText: "Promote" }).click();
    await expect(page.getByText(new RegExp(`promote:\\s*${escapeRegExp(euda.name)}`))).toBeVisible();
    await page.locator("button.btn-success").filter({ hasText: "Promote" }).click();

    await expect(page.getByText(new RegExp(`Successfully promoted ${escapeRegExp(euda.name)}`))).toBeVisible();

    // Once promoted the EUDA is no longer offered for promotion (either flagged or dropped).
    const after = await getEndUserApps(baseURL, token);
    expect(after.some(e => e.id === euda.id && !e.isPromoted), "euda no longer promotable").toBe(false);
});

/**
 * Scenario 12 — Static panels render, including the section help footer.
 * Static panels are CMS content stored via the API (not seeded by sample data), so the test
 * seeds two panels: a HOME panel (rendered on /home) and a SECTION.HELP panel for the Bookmarks
 * section (rendered as the dynamic-section help footer on an application view). The static-panel
 * API has no delete route, so the seeded panels are left in place (uniquely named per run).
 */
test("static panels and the section help footer render", async ({ page, context }) => {
    const token = await login(baseURL);

    const marker = uniqueName("e2e-panel");
    await createStaticPanel(baseURL, token, {
        group: "HOME",
        title: marker,
        icon: "info",
        content: `<p>${marker} home panel</p>`,
        priority: 1
    });

    const helpMarker = uniqueName("e2e-help");
    await createStaticPanel(baseURL, token, {
        group: "SECTION.HELP.bookmarks-section",
        title: helpMarker,
        icon: "info",
        content: `${helpMarker} bookmarks help`,
        priority: 1
    });

    const app = await createApp(baseURL, token, uniqueName("e2e_app_panel"));
    await authenticate(context, token);

    // --- static panel on the home page
    await page.goto("/home");
    await expect(page.getByText(`${marker} home panel`)).toBeVisible();

    // --- section help footer: open the Bookmarks section on the application view
    await page.goto(`/application/${app.id}`);
    await page.getByTitle("Bookmarks related to this entity").click();

    // The section help lookup keys panels by group, so concurrent runs seeding the same
    // SECTION.HELP group collapse to one; assert the footer renders with the shared help text
    // rather than this run's unique marker.
    const helpFooter = page.locator(".waltz-dynamic-section-help");
    await expect(helpFooter).toBeVisible();
    await expect(helpFooter).toContainText("bookmarks help");
});

/** Escape a string for safe use inside a RegExp (EUDA/panel names contain generated tokens). */
function escapeRegExp(s) {
    return s.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}
