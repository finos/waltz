import { expect } from "@playwright/test";
import { apiContext } from "./api.js";

/**
 * Measurable-rating seeding + UI helpers.
 *
 * To rate an application against a measurable you need an EDITABLE category that
 * holds a CONCRETE measurable, plus the rating-edit role. The sample data has
 * neither for `admin`, so we seed our own:
 *   1. grant TAXONOMY_EDITOR (create measurables) + RATING_EDITOR (rate);
 *   2. create an editable category (auto-creates an abstract Root measurable);
 *   3. bulk-apply one concrete measurable under that Root;
 * after which admin can add/edit/remove ratings, comments, decommission dates
 * and replacement apps. Allocations remain unreachable (no allocation-scheme
 * create endpoint — see the measurables spec fixme).
 */

/** Grant the taxonomy/rating roles, preserving existing roles (never REPLACE). */
export async function grantMeasurableRoles(ctx) {
    const who = await (await ctx.get("/api/user/whoami")).json();
    const roles = new Set([...(who.roles ?? []), "TAXONOMY_EDITOR", "RATING_EDITOR"]);
    const resp = await ctx.post(`/api/user/${who.userName}/roles`, {
        data: { roles: [...roles], comment: "waltz-e2e measurable tests" }
    });
    if (!resp.ok()) {
        throw new Error(`grant roles failed: ${resp.status()} ${await resp.text()}`);
    }
}

/** A rating scheme id whose items include user-selectable codes (e.g. G/A/R). */
export async function firstRatingSchemeId(ctx) {
    const schemes = await (await ctx.get("/api/rating-scheme")).json();
    return schemes[0].id;
}

/** Create an editable measurable category. Returns its id. */
export async function createEditableCategory(ctx, { name, externalId, ratingSchemeId }) {
    const resp = await ctx.post("/api/measurable-category/save", {
        data: {
            name,
            description: name,
            externalId,
            editable: true,
            ratingEditorRole: "RATING_EDITOR",
            ratingSchemeId,
            allowPrimaryRatings: true,
            isDeprecated: false,
            icon: "star",
            lastUpdatedBy: "admin"
        }
    });
    if (!resp.ok()) {
        throw new Error(`create category failed: ${resp.status()} ${await resp.text()}`);
    }
    return resp.json();
}

/** The measurables belonging to a category. */
export async function measurablesForCategory(ctx, categoryId) {
    const all = await (await ctx.get("/api/measurable/all")).json();
    return all.filter(m => m.categoryId === categoryId);
}

/**
 * Add one concrete measurable under a category's Root via taxonomy bulk-apply
 * (requires TAXONOMY_EDITOR). Returns the created measurable.
 */
export async function addConcreteMeasurable(ctx, categoryId, { name, externalId, parentExternalId }) {
    const tsv = `name\texternalId\tparentExternalId\tdescription\tconcrete\n${name}\t${externalId}\t${parentExternalId}\t${name}\ttrue\n`;
    const resp = await ctx.post(
        `/api/taxonomy-management/bulk/apply/MEASURABLE_CATEGORY/${categoryId}?format=TSV&mode=ADD_ONLY`,
        { headers: { "content-type": "text/plain" }, data: tsv }
    );
    if (!resp.ok()) {
        throw new Error(`bulk apply failed: ${resp.status()} ${await resp.text()}`);
    }
    const measurables = await measurablesForCategory(ctx, categoryId);
    const created = measurables.find(m => m.externalId === externalId);
    if (!created) {
        throw new Error(`concrete measurable ${externalId} not found after bulk apply`);
    }
    return created;
}

/**
 * Seed an editable category with an abstract Root and one concrete leaf under
 * it. Returns ids/names for both plus the rating scheme.
 */
export async function seedRatableTarget(baseURL, token, uniqueName) {
    const ctx = await apiContext(baseURL, token);
    try {
        await grantMeasurableRoles(ctx);
        const ratingSchemeId = await firstRatingSchemeId(ctx);

        const externalId = uniqueName("E2E_MCAT");
        const categoryName = uniqueName("e2e measurables");
        const categoryId = await createEditableCategory(ctx, {
            name: categoryName,
            externalId,
            ratingSchemeId
        });

        const root = (await measurablesForCategory(ctx, categoryId)).find(m => !m.concrete);
        const leaf = await addConcreteMeasurable(ctx, categoryId, {
            name: uniqueName("Concrete Leaf"),
            externalId: uniqueName("E2E_LEAF"),
            parentExternalId: root.externalId
        });

        return {
            categoryId,
            categoryName,
            ratingSchemeId,
            rootId: root.id,
            rootName: root.name,
            measurableId: leaf.id,
            measurableName: leaf.name,
            measurableExternalId: leaf.externalId
        };
    } finally {
        await ctx.dispose();
    }
}

/** Save a rating code (e.g. "G") for an app against a measurable. */
export async function saveRating(ctx, appId, measurableId, code) {
    const resp = await ctx.post(
        `/api/measurable-rating/entity/APPLICATION/${appId}/measurable/${measurableId}/rating`,
        { headers: { "content-type": "text/plain" }, data: code }
    );
    if (!resp.ok()) {
        throw new Error(`save rating failed: ${resp.status()} ${await resp.text()}`);
    }
    return resp.json();
}

/** The measurable ratings recorded for an application. */
export async function ratingsForApp(ctx, appId) {
    const resp = await ctx.get(`/api/measurable-rating/entity/APPLICATION/${appId}`);
    if (!resp.ok()) {
        throw new Error(`list ratings failed: ${resp.status()} ${await resp.text()}`);
    }
    return resp.json();
}

/** The rating code for a given app+measurable, or null. */
export async function ratingCode(ctx, appId, measurableId) {
    const ratings = await ratingsForApp(ctx, appId);
    const match = ratings.find(r => r.measurableId === measurableId);
    return match ? match.rating : null;
}

/** The measurable-rating row id for an app+measurable (needed for decommission). */
export async function ratingId(ctx, appId, measurableId) {
    const ratings = await ratingsForApp(ctx, appId);
    const match = ratings.find(r => r.measurableId === measurableId);
    return match ? match.id : null;
}

/** Add a planned decommission date to a measurable rating. Returns its id. */
export async function addDecommission(ctx, measurableRatingId, date) {
    const resp = await ctx.post(
        `/api/measurable-rating-planned-decommission/measurable-rating/${measurableRatingId}`,
        { data: { newVal: date, oldVal: null } }
    );
    if (!resp.ok()) {
        throw new Error(`add decommission failed: ${resp.status()} ${await resp.text()}`);
    }
    const body = await resp.json();
    return body.id;
}

// --- UI helpers ------------------------------------------------------------

/** Open an app's "Ratings / Roadmaps" section from the entity sidebar. */
export async function openRatingsSection(page, appId) {
    await page.goto(`/application/${appId}`);
    await page.getByRole("button", { name: "Ratings / Roadmap" }).click();
}

/** The measurable-rating entity section component on the entity page. */
export function ratingSection(page) {
    return page.locator("waltz-measurable-rating-entity-section");
}

/**
 * Enter rating edit mode. A rated entity offers an "Edit" link; a fresh one
 * offers "Add some ratings." — both toggle the editor. Handle either.
 */
export async function enterRatingsEdit(page) {
    const section = ratingSection(page);
    const edit = section.getByText("Edit", { exact: true });
    const add = section.getByText("Add some ratings.");
    await expect(edit.or(add).first()).toBeVisible();
    if (await edit.count() > 0) {
        await edit.first().click();
    } else {
        await add.first().click();
    }
}

/** Expand all collapsed branches in a ratings tree (to reveal leaf overlays). */
export async function expandRatingTree(page) {
    for (let i = 0; i < 6; i++) {
        const toggle = page.locator("li.tree-collapsed > .tree-branch-head").first();
        if (await toggle.count() === 0) {
            break;
        }
        await toggle.click();
    }
}

/** Select a category tab in the ratings edit panel by name. */
export async function selectCategoryTab(page, categoryName) {
    const panel = page.locator("waltz-measurable-rating-edit-panel");
    const tab = panel.locator("label.wt-label").filter({ hasText: categoryName });
    if (await tab.count() > 0) {
        await tab.first().click();
    }
}

/**
 * In the ratings edit panel, select a concrete measurable in the tree by name.
 */
export async function selectMeasurableInTree(page, measurableName) {
    await page.locator(".wmt-search-region input[type=search]").fill(measurableName);
    const leaf = page.locator(".wmrt-label").filter({ hasText: measurableName });
    // An unrated leaf sits under a collapsed ancestor (only rated ancestors
    // auto-expand). Expand collapsed branches until the leaf is visible.
    for (let i = 0; i < 6; i++) {
        if (await leaf.count() > 0 && await leaf.first().isVisible()) {
            break;
        }
        const toggle = page.locator("li.tree-collapsed > .tree-branch-head").first();
        if (await toggle.count() === 0) {
            break;
        }
        await toggle.click();
    }
    await leaf.first().click();
}

/**
 * Set the planned decommission date via the editable-field. Its "Set" trigger
 * is hover-revealed, and edit mode shows a date picker + Save button.
 */
export async function setDecommissionDateViaUi(page, date) {
    const field = page.locator("waltz-planned-decommission-editor waltz-editable-field").first();
    await field.hover();
    await field.getByText("Set", { exact: true }).click();
    await field.locator("input").first().fill(date);
    await field.locator("input").first().press("Escape");
    await field.getByRole("button", { name: "Save" }).click();
}

/** Pick a rating in the rating picker by its display name (e.g. "Good"). */
export async function pickRating(page, ratingName) {
    await page.locator(".waltz-rating-picker .wrp-option")
        .filter({ hasText: ratingName })
        .locator("label")
        .click();
}
