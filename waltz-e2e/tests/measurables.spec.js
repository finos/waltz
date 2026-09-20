import { test, expect } from "@playwright/test";
import { apiContext, authenticate, createApp, login, uniqueName } from "./helpers/api.js";
import * as ms from "./helpers/measurable.js";

const baseURL = process.env.WALTZ_BASE_URL ?? "http://localhost:8080";

/**
 * Measurable ratings e2e.
 *
 * A measurable rating attaches a rating-scheme item (e.g. "Good"/"Adequate") from a
 * measurable *category* to an application, against a concrete measurable in that category.
 *
 * The sample data has no editable category with a concrete measurable that `admin` may rate,
 * so each test seeds its own: grant TAXONOMY_EDITOR + RATING_EDITOR, create an editable
 * category (auto-creates an abstract Root), then bulk-apply one concrete measurable under the
 * Root. See helpers/measurable.js.
 */

test("Ratings / Roadmaps section renders the Ratings sub-section for an API-seeded app", async ({ page, context }) => {
    const token = await login(baseURL);
    const app = await createApp(baseURL, token, uniqueName("ms_app"));

    await authenticate(context, token);
    await ms.openRatingsSection(page, app.id);

    await expect(ms.ratingSection(page)).toBeVisible();
});

test("adds, edits and removes a measurable rating", async ({ page, context }) => {
    const token = await login(baseURL);
    const target = await ms.seedRatableTarget(baseURL, token, uniqueName);
    const app = await createApp(baseURL, token, uniqueName("ms_rate"));
    const ctx = await apiContext(baseURL, token);

    await authenticate(context, token);
    // Native confirm() is used by "Remove all mappings".
    page.on("dialog", d => d.accept());
    await ms.openRatingsSection(page, app.id);

    // Enter edit mode, pick the concrete measurable, choose a rating.
    await ms.enterRatingsEdit(page);
    await ms.selectCategoryTab(page, target.categoryName);
    await ms.selectMeasurableInTree(page, target.measurableName);
    await ms.pickRating(page, "Good");
    await expect.poll(() => ms.ratingCode(ctx, app.id, target.measurableId)).toBe("G");

    // Edit the rating to a different value (re-select the node after the save reload).
    await ms.selectMeasurableInTree(page, target.measurableName);
    await ms.pickRating(page, "Adequate");
    await expect.poll(() => ms.ratingCode(ctx, app.id, target.measurableId)).toBe("A");

    // Remove the rating (Remove all mappings for the category).
    await page.getByText("Remove all mappings").click();
    await expect.poll(() => ms.ratingCode(ctx, app.id, target.measurableId)).toBeNull();

    await ctx.dispose();
});

test("adds and updates a comment on a measurable rating", async ({ page, context }) => {
    const token = await login(baseURL);
    const target = await ms.seedRatableTarget(baseURL, token, uniqueName);
    const app = await createApp(baseURL, token, uniqueName("ms_comment"));
    const ctx = await apiContext(baseURL, token);
    // Rating must exist before a comment can be attached.
    await ms.saveRating(ctx, app.id, target.measurableId, "G");

    await authenticate(context, token);
    await ms.openRatingsSection(page, app.id);
    await ms.enterRatingsEdit(page);
    await ms.selectCategoryTab(page, target.categoryName);
    await ms.selectMeasurableInTree(page, target.measurableName);

    // The comment editor is a waltz-inline-edit-area in the rating editor panel.
    const commentArea = page.locator("waltz-inline-edit-area").first();
    await commentArea.getByText("Edit").click();
    await commentArea.locator("textarea").fill("initial comment");
    await commentArea.getByRole("button", { name: "Save" }).click();
    await expect.poll(async () => {
        const r = (await ms.ratingsForApp(ctx, app.id)).find(x => x.measurableId === target.measurableId);
        return r?.description;
    }).toBe("initial comment");

    // Update it.
    await commentArea.getByText("Edit").click();
    await commentArea.locator("textarea").fill("updated comment");
    await commentArea.getByRole("button", { name: "Save" }).click();
    await expect.poll(async () => {
        const r = (await ms.ratingsForApp(ctx, app.id)).find(x => x.measurableId === target.measurableId);
        return r?.description;
    }).toBe("updated comment");

    await ctx.dispose();
});

test("adds and revokes a decommission date with its icon overlay", async ({ page, context }) => {
    const token = await login(baseURL);
    const target = await ms.seedRatableTarget(baseURL, token, uniqueName);
    const app = await createApp(baseURL, token, uniqueName("ms_decomm"));
    const ctx = await apiContext(baseURL, token);
    await ms.saveRating(ctx, app.id, target.measurableId, "G");

    await authenticate(context, token);
    // Revoke uses a native confirm().
    page.on("dialog", d => d.accept());
    await ms.openRatingsSection(page, app.id);
    await ms.enterRatingsEdit(page);
    await ms.selectCategoryTab(page, target.categoryName);
    await ms.selectMeasurableInTree(page, target.measurableName);

    // The decommission editor appears once a rating exists. Set a date.
    const decommEditor = page.locator("waltz-planned-decommission-editor");
    await ms.setDecommissionDateViaUi(page, "2031-06-01");

    // Decommission (no replacement) overlay appears on the tree node.
    await expect(page.locator(".wmrt-node .fa-hand-paper-o").first()).toBeVisible();
    await expect.poll(async () => {
        const decs = await (await ctx.get(`/api/measurable-rating-planned-decommission/entity/APPLICATION/${app.id}`)).json();
        return decs.length;
    }).toBeGreaterThan(0);

    // Revoke it — re-select the node (the editor collapses after the save) then
    // the decommission is removed and the overlay disappears.
    await ms.selectMeasurableInTree(page, target.measurableName);
    await decommEditor.getByText("Revoke the decommissioning").click();
    await expect.poll(async () => {
        const decs = await (await ctx.get(`/api/measurable-rating-planned-decommission/entity/APPLICATION/${app.id}`)).json();
        return decs.length;
    }).toBe(0);
    await ms.openRatingsSection(page, app.id);
    await expect(page.locator(".wmrt-node .fa-hand-paper-o")).toHaveCount(0);

    await ctx.dispose();
});

test("adds and removes a replacement application with its icon overlay", async ({ page, context }) => {
    const token = await login(baseURL);
    const target = await ms.seedRatableTarget(baseURL, token, uniqueName);
    const app = await createApp(baseURL, token, uniqueName("ms_repl_src"));
    const replacement = await createApp(baseURL, token, uniqueName("ms_repl_dst"));
    const ctx = await apiContext(baseURL, token);
    // Seed rating + decommission via REST; drive replacement add/remove via UI.
    await ms.saveRating(ctx, app.id, target.measurableId, "G");
    const rid = await ms.ratingId(ctx, app.id, target.measurableId);
    await ms.addDecommission(ctx, rid, "2031-06-01");

    await authenticate(context, token);
    // Replacement removal uses a native confirm().
    page.on("dialog", d => d.accept());
    await ms.openRatingsSection(page, app.id);
    await ms.enterRatingsEdit(page);
    await ms.selectCategoryTab(page, target.categoryName);
    await ms.selectMeasurableInTree(page, target.measurableName);

    const decommEditor = page.locator("waltz-planned-decommission-editor");
    // Add a replacement app: Add (link) -> pick app -> commission date (the field
    // starts in edit mode) -> Save -> Confirm.
    await decommEditor.getByText("Add", { exact: true }).click();
    await decommEditor.locator(".ui-select-match").click();
    await page.locator("input.ui-select-search:visible").fill(replacement.name);
    await page.locator(".ui-select-choices-row", { hasText: replacement.name }).first().click();
    await decommEditor.locator("input").first().fill("2031-07-01");
    await decommEditor.locator("input").first().press("Escape");
    await decommEditor.getByRole("button", { name: "Save" }).click();
    await decommEditor.getByRole("button", { name: "Confirm" }).click();

    // Decommission-with-replacement overlay on the source app's tree.
    await expect(page.locator(".wmrt-node .fa-hand-o-right").first()).toBeVisible();

    // The replacement app's own tree shows the incoming ("handshake") overlay.
    // Its leaf is unrated, so expand the tree to reveal the node.
    await ms.openRatingsSection(page, replacement.id);
    await ms.expandRatingTree(page);
    await expect(page.locator(".wmrt-node .fa-handshake-o").first()).toBeVisible();

    // Remove the replacement (back on the source app), overlay reverts.
    await ms.openRatingsSection(page, app.id);
    await ms.enterRatingsEdit(page);
    await ms.selectCategoryTab(page, target.categoryName);
    await ms.selectMeasurableInTree(page, target.measurableName);
    await decommEditor.getByText("Remove").first().click();
    await decommEditor.getByRole("button", { name: "Confirm" }).click();
    await expect(page.locator(".wmrt-node .fa-hand-o-right")).toHaveCount(0);

    await ctx.dispose();
});

test("rolls a leaf rating up to its ancestor (inferred relationship)", async ({ page, context }) => {
    const token = await login(baseURL);
    const target = await ms.seedRatableTarget(baseURL, token, uniqueName);
    const app = await createApp(baseURL, token, uniqueName("ms_rollup"));
    const ctx = await apiContext(baseURL, token);
    // Rate the concrete leaf only; the abstract Root ancestor is never rated directly.
    await ms.saveRating(ctx, app.id, target.measurableId, "G");

    // API check: the rating is inferred onto the ancestor (CHILDREN) but absent for EXACT.
    const childrenHit = await (await ctx.post("/api/measurable-rating/measurable-selector", {
        data: { entityReference: { kind: "MEASURABLE", id: target.rootId }, scope: "CHILDREN" }
    })).json();
    expect(childrenHit.some(r => r.entityReference.id === app.id && r.measurableId === target.measurableId)).toBe(true);
    const exactHit = await (await ctx.post("/api/measurable-rating/measurable-selector", {
        data: { entityReference: { kind: "MEASURABLE", id: target.rootId }, scope: "EXACT" }
    })).json();
    expect(exactHit.some(r => r.entityReference.id === app.id)).toBe(false);

    // UI check: the ancestor measurable's Ratings section lists the app via roll-up.
    await authenticate(context, token);
    await page.goto(`/measurable/${target.rootId}`);
    // Match the "Ratings" section button exactly (avoid "Assessment Ratings");
    // section nav names carry a leading space.
    await page.locator("button").filter({ hasText: /^\s*Ratings\s*$/ }).first().click();
    await expect(page.getByText(app.name).first()).toBeVisible();

    await ctx.dispose();
});

/**
 * Allocations require an allocation scheme tied to the category, but there is NO
 * REST endpoint to create one (AllocationSchemeEndpoint is read-only) and the
 * sample data seeds none. The "Allocations" sub-section only appears when a
 * scheme exists, so add/remove allocations and the allocation icon overlay
 * cannot be exercised on the ephemeral backend.
 */
test.fixme(
    "adds and removes allocations",
    async () => {
        // Blocked: no create endpoint for allocation schemes (allocation-scheme
        // API is read-only) and none are seeded, so the Allocations editor never
        // renders. Enable when the environment provides an allocation scheme.
    }
);
