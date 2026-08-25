import { test, expect } from "@playwright/test";
import { authenticate, createApp, login, uniqueName } from "./helpers/api.js";

const baseURL = process.env.WALTZ_BASE_URL ?? "http://localhost:8080";

/**
 * Applications area e2e (issue #6071).
 *
 * All setup is seeded via the REST API (createApp -> POST /api/app); the tests then
 * drive the AngularJS/Svelte UI. The app overview is the default landing section of
 * /application/{id}, which renders the Tags (waltz-tag-list / waltz-tag-edit) and the
 * Aliases (Svelte AliasControl) controls, and links to the edit form (/application/{id}/edit).
 */

/**
 * Scenario 1: Add and remove a tag on an application.
 * Tags live in the app overview's ".waltz-tags-list" region. Editing swaps waltz-tag-list
 * for waltz-tag-edit (ngTagsInput); adding/removing a tag auto-saves via TagStore.update.
 */
test("add and remove a tag on an application", async ({ page, context }) => {
    const token = await login(baseURL);
    const app = await createApp(baseURL, token, uniqueName("ts_tag_app"));
    const tagName = uniqueName("ts_tag");

    await authenticate(context, token);
    await page.goto(`/application/${app.id}`);

    const tagsRegion = page.locator(".waltz-tags-list");
    await expect(tagsRegion).toBeVisible();

    // Open the tag editor ("add one." when empty, "update" otherwise).
    // These are ng-click anchors without href, so match on their text.
    await tagsRegion.locator("a.clickable").filter({ hasText: /add one|update/ }).first().click();

    // Add a tag: type into the ngTagsInput and press Enter (auto-saves).
    const tagInput = tagsRegion.locator("waltz-tag-edit tags-input input");
    await tagInput.fill(tagName);
    await tagInput.press("Enter");

    // The new tag chip should appear inside the editor's tag list.
    await expect(tagsRegion.locator(".tag-item").filter({ hasText: tagName })).toBeVisible();

    // Dismiss the autocomplete dropdown (it overlays the Close button).
    await tagInput.press("Escape");

    // Close the editor and verify the tag is shown in the read-only keyword list.
    await tagsRegion.locator("waltz-tag-edit button").filter({ hasText: "Close" }).dispatchEvent("click");
    await expect(
        tagsRegion.locator(".waltz-keyword-list .wkl-keyword").filter({ hasText: tagName })
    ).toBeVisible();

    // Now remove it: reopen the editor and delete the chip (auto-saves).
    await tagsRegion.locator("a.clickable").filter({ hasText: "update" }).first().click();
    const chip = tagsRegion.locator(".tag-item").filter({ hasText: tagName });
    // The ngTagsInput text field overlaps the small "×" remove button, so dispatch the
    // click directly on the element (a real click lands on the overlapping input).
    await chip.locator(".remove-button").dispatchEvent("click");
    await expect(chip).toHaveCount(0);

    await tagInput.press("Escape");
    await tagsRegion.locator("waltz-tag-edit button").filter({ hasText: "Close" }).dispatchEvent("click");

    // Back in read-only view the tag should be gone (empty state re-appears).
    await expect(
        tagsRegion.locator(".waltz-keyword-list .wkl-keyword").filter({ hasText: tagName })
    ).toHaveCount(0);
});

/**
 * Scenario 2: Edit an application (description + business criticality) and verify it persists.
 * Uses the Svelte edit form at /application/{id}/edit which PUTs the change set, then
 * reloading the overview should reflect the new values.
 */
test("edit an application and verify the change persists", async ({ page, context }) => {
    const token = await login(baseURL);
    const app = await createApp(baseURL, token, uniqueName("ts_edit_app"));
    const newDescription = uniqueName("ts_desc");

    await authenticate(context, token);
    // Land on the view page first so the edit form's history.back() returns here on save.
    await page.goto(`/application/${app.id}`);
    await expect(page.locator(".waltz-page-summary").first()).toBeVisible();
    await page.goto(`/application/${app.id}/edit`);

    // Wait for the form to hydrate with the existing app data.
    const nameInput = page.locator("#name");
    await expect(nameInput).toHaveValue(app.name);

    // Change description and business criticality (seeded as MEDIUM -> HIGH).
    await page.locator("#description").fill(newDescription);
    await page.locator("#criticality").selectOption("HIGH");

    await page.getByRole("button", { name: "Save" }).click();

    // Saving navigates back (history.back()) to the app view. Assert the overview reflects
    // the persisted changes.
    await page.waitForURL(`**/application/${app.id}`);

    const summary = page.locator(".waltz-page-summary").first();
    await expect(summary.getByText(newDescription)).toBeVisible();
    await expect(summary.getByText("High", { exact: true })).toBeVisible();

    // Double-check persistence with a fresh load.
    await page.goto(`/application/${app.id}`);
    await expect(page.locator(".waltz-page-summary").first().getByText(newDescription)).toBeVisible();
});

/**
 * Scenario 3: Add an alias to an application and verify it is searchable via global search.
 * The overview's Svelte AliasControl edits aliases; ApplicationSearchDao joins ENTITY_ALIAS,
 * so the alias term should surface the application in global search.
 */
test("add an alias and find the application by it in global search", async ({ page, context }) => {
    const token = await login(baseURL);
    const app = await createApp(baseURL, token, uniqueName("ts_alias_app"));
    const aliasName = uniqueName("tsalias");

    await authenticate(context, token);
    await page.goto(`/application/${app.id}`);

    // Open the alias editor, add the alias, and save.
    const aliasControl = page.locator(".waltz-alias-list");
    await expect(aliasControl).toBeVisible();
    await aliasControl.getByRole("button", { name: "Edit" }).click();

    const aliasInput = aliasControl.locator("input");
    await aliasInput.fill(aliasName);
    await aliasInput.press("Enter");
    await aliasControl.getByRole("button", { name: "Save" }).click();

    // Back in view mode the alias chip should be listed.
    await expect(aliasControl.locator("li.tag").filter({ hasText: aliasName })).toBeVisible();

    // Now search globally by the alias and confirm the seeded app is found.
    await page.locator(".navbar-right").getByTestId("search-button").click();
    const searchRegion = page.locator(".wnso-search-region");
    await searchRegion.locator("input[type=search]").fill(aliasName);

    const result = page
        .locator(".wnso-search-results")
        .getByTestId("entity-name")
        .getByText(app.name);
    await expect(result).toBeVisible();
});
