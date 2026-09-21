import { test, expect } from "@playwright/test";
import { authenticate, login, uniqueName } from "./helpers/api.js";
import {
    createAppGroup, getChangeInitiatives, getEntityRelationships,
    createEntityRelationship, exportEntityRelationships
} from "./helpers/entity-relationship.js";

const baseURL = process.env.WALTZ_BASE_URL ?? "http://localhost:8080";

/**
 * Entity Relationships, exercised through the specific-context editors that surface them rather
 * than the generic subsystem. The "Groups" section (waltz-related-app-groups-section) lets an
 * app group or a change initiative be related to an app group via a RELATES_TO entity relationship
 * (POST/DELETE /api/entity-relationship). Data is seeded via the REST API and the create/remove is
 * driven and verified through the UI.
 */

// The "Groups" dynamic section: its custom element and the sidebar button title used to open it.
const GROUPS_SECTION = "waltz-related-app-groups-section";
const GROUPS_TITLE = "Application groups with a direct relationship to this entity";

/**
 * Add a counterpart in an open "Groups" editor: enter edit mode, pick the target in the entity
 * selector (ui-select typeahead), then Add. Assumes the section is already open on the page.
 */
async function addGroupRelationship(page, section, counterpartName) {
    await section.getByRole("button", { name: "Edit" }).click();
    await section.locator(".ui-select-match").click();
    // The dropdown search box is appended to the page; scope to the visible one.
    await page.locator("input.ui-select-search:visible").fill(counterpartName);
    await page.locator(".ui-select-choices-row").filter({ hasText: counterpartName }).click();
    // "Add" / "Remove" are anchors without an href, so they carry no link role — match by text.
    await section.locator("a.btn-primary").filter({ hasText: "Add" }).click();
}

/**
 * Scenario 1 — Relate and unrelate two app groups via the app group's "Groups" section.
 */
test("relate and unrelate two app groups via the Groups section", async ({ page, context }) => {
    const token = await login(baseURL);
    const groupA = await createAppGroup(baseURL, token, uniqueName("e2e Group A"));
    const groupB = await createAppGroup(baseURL, token, uniqueName("e2e Group B"));
    await authenticate(context, token);

    await page.goto(`/app-group/${groupA.id}`);
    await page.getByTitle(GROUPS_TITLE).click();
    const section = page.locator(GROUPS_SECTION);

    // --- create
    await addGroupRelationship(page, section, groupB.name);
    await expect(page.getByText("Relationship created")).toBeVisible();
    expect((await getEntityRelationships(baseURL, token, "APP_GROUP", groupA.id))
        .some(r => r.b.id === groupB.id && r.relationship === "RELATES_TO"), "relationship created").toBe(true);

    const row = section.locator("tr").filter({ hasText: groupB.name });
    await expect(row).toBeVisible();

    // --- remove (the control is hover-revealed)
    await row.hover();
    await row.locator("a").filter({ hasText: "Remove" }).click();
    await expect(page.getByText("Relationship removed")).toBeVisible();
    expect((await getEntityRelationships(baseURL, token, "APP_GROUP", groupA.id))
        .some(r => r.b.id === groupB.id), "relationship removed").toBe(false);
});

/**
 * Scenario 2 — Relate and unrelate an app group to a change initiative via the CI's "Groups" section.
 * The same editor is reachable from the change-initiative view (a distinct use case / context).
 */
test("relate and unrelate an app group to a change initiative", async ({ page, context }, testInfo) => {
    const token = await login(baseURL);
    const cis = await getChangeInitiatives(baseURL, token);
    const ci = cis[testInfo.parallelIndex % cis.length];
    const group = await createAppGroup(baseURL, token, uniqueName("e2e CI Group"));
    await authenticate(context, token);

    await page.goto(`/change-initiative/${ci.id}`);
    await page.getByTitle(GROUPS_TITLE).click();
    const section = page.locator(GROUPS_SECTION);

    const involvesGroup = rels => rels.some(r =>
        (r.b.id === group.id || r.a.id === group.id) && r.relationship === "RELATES_TO");

    // --- create
    await addGroupRelationship(page, section, group.name);
    await expect(page.getByText("Relationship created")).toBeVisible();
    expect(involvesGroup(await getEntityRelationships(baseURL, token, "CHANGE_INITIATIVE", ci.id)),
        "relationship created").toBe(true);

    // --- remove
    const row = section.locator("tr").filter({ hasText: group.name });
    await row.hover();
    await row.locator("a").filter({ hasText: "Remove" }).click();
    await expect(page.getByText("Relationship removed")).toBeVisible();
    expect(involvesGroup(await getEntityRelationships(baseURL, token, "CHANGE_INITIATIVE", ci.id)),
        "relationship removed").toBe(false);
});

/**
 * Scenario 3 — Export an entity's relationships as CSV.
 * The "Groups" editor has no export control (export lives in the data-extract endpoint the
 * viewpoints "Download all relationships" link uses); this seeds a relationship and asserts the
 * extract contains it.
 */
test("export entity relationships as CSV", async () => {
    const token = await login(baseURL);
    const groupA = await createAppGroup(baseURL, token, uniqueName("e2e Export A"));
    const groupB = await createAppGroup(baseURL, token, uniqueName("e2e Export B"));
    await createEntityRelationship(baseURL, token, "APP_GROUP", groupA.id, "APP_GROUP", groupB.id);

    const csv = await exportEntityRelationships(baseURL, token, "APP_GROUP", groupA.id, "CSV");
    expect(csv).toContain("relationship_kind");
    expect(csv).toContain("RELATES_TO");
    expect(csv).toContain(groupB.name);
});
