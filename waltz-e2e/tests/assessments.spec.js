import { test, expect } from "@playwright/test";
import { apiContext, authenticate, createApp, login, uniqueName } from "./helpers/api.js";

const baseURL = process.env.WALTZ_BASE_URL ?? "http://localhost:8080";

/**
 * Assessments — create / update / delete ratings (issue #6071 checklist item).
 *
 * Seeding notes:
 *  - All baseline APPLICATION assessment definitions in the sample data are
 *    `isReadOnly: true`, which the permission checker maps to *no* write
 *    operations (see AssessmentRatingDao.determineOperations -> if defIsReadOnly
 *    return emptySet()). So we cannot rate against any of them.
 *  - Contrary to the README's "delete+read only" note, AssessmentDefinitionEndpoint
 *    DOES expose a write route: PUT /api/assessment-definition (guarded by
 *    ADMIN / ASSESSMENT_DEFINITION_ADMIN, both held by the admin user). We use it
 *    to seed a writable (isReadOnly:false, permittedRole:null) APPLICATION
 *    definition, which grants admin ADD/UPDATE/REMOVE without any extra role or
 *    involvement. This is REST-API seeding, consistent with the repo rules.
 *
 * The three rating operations are then driven entirely through the embedded
 * assessments section UI (/embed/internal/APPLICATION/{id}/200), mirroring the
 * flow in the Java AssessmentCreationAndRemovalIntegrationTest (whose locators /
 * "Information Classification" definition have bit-rotted).
 */

const ASSESSMENTS_SECTION_ID = 200;

/**
 * Pick a rating scheme (dynamically) with at least `minSelectable` user-selectable ratings.
 * Returns the scheme id plus its selectable ratings as {name, code} (code == the single-char
 * `rating` value the bulk import keys on).
 */
async function pickWritableScheme(ctx, minSelectable = 1) {
    const schemes = await (await ctx.get("/api/rating-scheme")).json();
    for (const scheme of schemes) {
        const selectable = (scheme.ratings ?? []).filter(r => r.userSelectable === true);
        if (selectable.length >= minSelectable) {
            return { schemeId: scheme.id, ratings: selectable.map(r => ({ name: r.name, code: r.rating })) };
        }
    }
    throw new Error(`no rating scheme with ${minSelectable} user-selectable rating(s) found`);
}

/**
 * Seed a writable APPLICATION assessment definition via PUT /api/assessment-definition.
 * The body must carry lastUpdatedBy/At (required immutable fields) even though the
 * endpoint overrides them server-side. Returns the new definition id.
 */
async function createAssessmentDefinition(ctx, name, schemeId, { isReadOnly = false } = {}) {
    const resp = await ctx.put("/api/assessment-definition", {
        data: {
            name,
            description: "waltz-e2e writable assessment",
            entityKind: "APPLICATION",
            ratingSchemeId: schemeId,
            permittedRole: null,
            visibility: "PRIMARY",
            definitionGroup: "Uncategorized",
            kind: "ASSESSMENT_DEFINITION",
            cardinality: "ZERO_ONE",
            isReadOnly,
            provenance: "waltz",
            lastUpdatedBy: "admin",
            lastUpdatedAt: "2020-01-01T00:00:00.000"
        }
    });
    if (!resp.ok()) {
        throw new Error(`create assessment definition failed: ${resp.status()} ${await resp.text()}`);
    }
    return Number(await resp.text());
}

/** Additively grant a role to admin (union with whoami, so parallel specs don't clobber roles). */
async function grantRole(token, role) {
    const ctx = await apiContext(baseURL, token);
    const who = await (await ctx.get("/api/user/whoami")).json();
    const roles = Array.from(new Set([...(who.roles || []), role]));
    const resp = await ctx.post(`/api/user/${who.userName}/roles`, {
        data: { roles, comment: "e2e assessments" }
    });
    if (!resp.ok()) {
        throw new Error(`grant role failed: ${resp.status()} ${await resp.text()}`);
    }
    await ctx.dispose();
}

/**
 * Expand the left-hand "Uncategorized" group and its "Not Rated" sub-list, returning the
 * group locator. Scoped to "Uncategorized" because other groups have their own toggles.
 */
async function openUncategorizedNotRated(page) {
    const group = page.locator("tbody.assessment-group").filter({ hasText: "Uncategorized" });

    // Expand the group if it is currently collapsed.
    const groupCaret = group.locator('[data-ux="Uncategorized-caret-right-button"]');
    if (await groupCaret.count() > 0) {
        await groupCaret.click();
    }

    // Unrated definitions live under a collapsed "Not Rated" sub-list.
    const notRated = group.getByRole("button", { name: "Not Rated" });
    if (await notRated.count() > 0) {
        await notRated.click();
    }
    return group;
}

/** Select an (editable) definition by its unique name so the right-hand editor panel loads it. */
async function selectAssessment(page, defName) {
    const group = await openUncategorizedNotRated(page);
    await group.getByRole("button", { name: defName, exact: true }).click();
}

/**
 * Build the bulk-editor TSV (header + rows). Columns match the parser
 * (BulkAssessmentRatingItemParser): externalId, ratingCode, isReadOnly, comment.
 */
function toTsv(rows) {
    const header = "externalId\tratingCode\tisReadOnly\tcomment";
    const body = rows.map(r => `${r.externalId}\t${r.code}\t${r.readOnly ?? "false"}\t${r.comment ?? ""}`);
    return [header, ...body].join("\n");
}

/** Seed ratings through the bulk-apply endpoint (raw TSV body), used to prime the export test. */
async function applyBulk(ctx, defId, tsv) {
    const resp = await ctx.post(`/api/assessment-rating/bulk/apply/ASSESSMENT_DEFINITION/${defId}`, {
        data: tsv,
        headers: { "Content-Type": "text/plain" }
    });
    if (!resp.ok()) {
        throw new Error(`bulk apply failed: ${resp.status()} ${await resp.text()}`);
    }
}


test.describe("assessments: bulk update ratings", () => {

    let token;
    let ctx;
    let defId;
    let ratings;
    let app1;
    let app2;

    test.beforeAll(async () => {
        // Bulk apply/preview require ASSESSMENT_DEFINITION_ADMIN (BulkAssessmentRatingService
        // .verifyUserHasPermissions), which admin lacks by default; grant it and re-login.
        await grantRole(await login(baseURL), "ASSESSMENT_DEFINITION_ADMIN");
        token = await login(baseURL);
        ctx = await apiContext(baseURL, token);
        // Two selectable ratings so we can bulk-ADD with one code, then bulk-UPDATE to another.
        const scheme = await pickWritableScheme(ctx, 2);
        ratings = scheme.ratings;
        defId = await createAssessmentDefinition(ctx, uniqueName("e2e Bulk Assessment"), scheme.schemeId);
        app1 = await createApp(baseURL, token, uniqueName("e2e_bulk_a"));
        app2 = await createApp(baseURL, token, uniqueName("e2e_bulk_b"));
    });

    test.afterAll(async () => {
        if (defId) {
            await ctx.delete(`/api/assessment-definition/id/${defId}`);
        }
        await ctx.dispose();
    });

    test("bulk add then bulk update ratings via TSV paste", async ({ page, context }) => {
        await authenticate(context, token);
        await page.goto(`/assessment-definition/${defId}/bulk-edit`);

        const rawText = page.locator("#rawText");
        await expect(rawText).toBeVisible();

        // --- Bulk ADD: rate both apps with the first rating code (matched on app external id). ---
        await rawText.fill(toTsv([
            { externalId: app1.name, code: ratings[0].code, comment: "bulk add 1" },
            { externalId: app2.name, code: ratings[0].code, comment: "bulk add 2" }
        ]));
        await page.getByRole("button", { name: "Preview", exact: true }).click();

        const preview = page.locator(".preview-table");
        await expect(preview).toContainText(app1.name);
        await expect(preview).toContainText(app2.name);

        await page.getByRole("button", { name: "Apply", exact: true }).click();
        await expect(page.locator("tr", { hasText: "Added Records" })).toContainText("2");

        // --- Bulk UPDATE: change both apps to the second rating code. ---
        await page.getByRole("button", { name: "Back to Bulk Editor" }).click();
        await rawText.fill(toTsv([
            { externalId: app1.name, code: ratings[1].code, comment: "bulk update 1" },
            { externalId: app2.name, code: ratings[1].code, comment: "bulk update 2" }
        ]));
        await page.getByRole("button", { name: "Preview", exact: true }).click();
        await page.getByRole("button", { name: "Apply", exact: true }).click();

        await expect(page.locator("tr", { hasText: "Updated Records" })).toContainText("2");
    });
});


test.describe("assessments: bulk export", () => {

    let token;
    let ctx;
    let defId;
    let app;

    test.beforeAll(async () => {
        // applyBulk (bulk apply) requires ASSESSMENT_DEFINITION_ADMIN; grant it and re-login.
        await grantRole(await login(baseURL), "ASSESSMENT_DEFINITION_ADMIN");
        token = await login(baseURL);
        ctx = await apiContext(baseURL, token);
        const scheme = await pickWritableScheme(ctx);
        defId = await createAssessmentDefinition(ctx, uniqueName("e2e Export Assessment"), scheme.schemeId);
        app = await createApp(baseURL, token, uniqueName("e2e_exp_app"));
        // Seed one rating so the export has a data row for our app.
        await applyBulk(ctx, defId, toTsv([{ externalId: app.name, code: scheme.ratings[0].code, comment: "export seed" }]));
    });

    test.afterAll(async () => {
        if (defId) {
            await ctx.delete(`/api/assessment-definition/id/${defId}`);
        }
        await ctx.dispose();
    });

    test("export assessment ratings as CSV from the definition view", async ({ page, context }) => {
        await authenticate(context, token);
        await page.goto(`/assessment-definition/${defId}`);

        // The "Export" data-extract dropdown lives in the definition view's section actions.
        const exportToggle = page.locator(".btn-group").filter({ hasText: "Export" }).first();
        await expect(exportToggle).toBeVisible();
        await exportToggle.click();

        const downloadPromise = page.waitForEvent("download");
        await page.getByText("Export as csv", { exact: true }).click();
        const download = await downloadPromise;

        const fs = await import("node:fs/promises");
        const contents = await fs.readFile(await download.path(), "utf8");
        expect(contents).toContain("External Id");     // extractor column header
        expect(contents).toContain(app.name);          // the rated app appears as a data row
    });
});


test.describe("assessments: permissions are respected", () => {

    let token;
    let ctx;
    let defId;
    let defName;

    test.beforeAll(async () => {
        token = await login(baseURL);
        ctx = await apiContext(baseURL, token);
        const scheme = await pickWritableScheme(ctx);
        defName = uniqueName("e2e ReadOnly Assessment");
        // A read-only definition yields NO write operations for anyone (AssessmentRatingDao
        // .determineOperations returns emptySet when defIsReadOnly), even for an ADMIN user.
        defId = await createAssessmentDefinition(ctx, defName, scheme.schemeId, { isReadOnly: true });
    });

    test.afterAll(async () => {
        if (defId) {
            await ctx.delete(`/api/assessment-definition/id/${defId}`);
        }
        await ctx.dispose();
    });

    test("a read-only assessment is listed but cannot be rated", async ({ page, context }) => {
        const app = await createApp(baseURL, token, uniqueName("e2e_ro_app"));

        await authenticate(context, token);
        await page.goto(`/embed/internal/APPLICATION/${app.id}/${ASSESSMENTS_SECTION_ID}`);

        const group = await openUncategorizedNotRated(page);

        // The definition is listed under "Not Rated"...
        await expect(group.getByText(defName)).toBeVisible();
        // ...but a read-only definition renders as plain text, not a clickable button
        // (AssessmentRatingListGroup only wraps writable defs in a select button), so there
        // is no way to open the editor and add a rating — the read-only permission is respected.
        await expect(group.getByRole("button", { name: defName, exact: true })).toHaveCount(0);
    });
});

test.describe("assessments: create / update / delete ratings", () => {

    let token;
    let ctx;
    let defId;
    let defName;
    let ratingName;
    let ratingName2;
    let appId;

    test.beforeAll(async () => {
        token = await login(baseURL);
        ctx = await apiContext(baseURL, token);
        // Two selectable ratings: one to create with, one to change the value to.
        const { schemeId, ratings } = await pickWritableScheme(ctx, 2);
        ratingName = ratings[0].name;
        ratingName2 = ratings[1].name;
        defName = uniqueName("e2e Assessment");
        defId = await createAssessmentDefinition(ctx, defName, schemeId);
    });

    test.afterAll(async () => {
        // Tidy up the seeded definition (also removes its ratings).
        if (defId) {
            await ctx.delete(`/api/assessment-definition/id/${defId}`);
        }
        await ctx.dispose();
    });

    test("rate an application: create, change value, update comment, then remove", async ({ page, context }) => {
        const app = await createApp(baseURL, token, uniqueName("e2e_assess_app"));
        appId = app.id;

        await authenticate(context, token);
        await page.goto(`/embed/internal/APPLICATION/${appId}/${ASSESSMENTS_SECTION_ID}`);

        // The editor panel is on the right; scope assertions to it to avoid the
        // left-hand definition list matching the same text.
        const editor = page.locator(".waltz-sticky-part");

        // -----------------------------------------------------------------
        // Scenario 1: create a rating (pick value + comment, save)
        // -----------------------------------------------------------------
        await selectAssessment(page, defName);

        await expect(editor.getByText(defName)).toBeVisible();

        await editor.getByRole("button", { name: "Add" }).click();

        await page.locator("#rating-dropdown").selectOption({ label: ratingName });
        const createComment = "Initial e2e comment";
        await page.locator("#comment").fill(createComment);
        await editor.getByRole("button", { name: "Save" }).click();

        // Back in the list view the rating (its value + truncated comment) is shown.
        await expect(editor.getByText(ratingName).first()).toBeVisible();
        await expect(editor.getByText(createComment)).toBeVisible();

        // Open the rating's detail view (shared by the value + comment edits below).
        await editor.locator("tr.clickable", { hasText: ratingName }).first().click();

        // -----------------------------------------------------------------
        // Scenario 2: change the rating VALUE (the selected option) to another rating.
        // The value editor (#rating) has its own pencil Edit that swaps in a <select> of
        // the OTHER available ratings; picking one saves immediately.
        // -----------------------------------------------------------------
        const ratingField = editor.locator("#rating");
        await ratingField.getByRole("button", { name: "Edit" }).click();
        await ratingField.locator("select").selectOption({ label: ratingName2 });

        // The value returns to view mode showing the newly selected rating.
        await expect(ratingField.getByText(ratingName2)).toBeVisible();

        // -----------------------------------------------------------------
        // Scenario 3: update the rating's comment
        // -----------------------------------------------------------------
        // Detail view: the comment lives in #comment with its own inline Edit control.
        const commentField = editor.locator("#comment");
        await commentField.getByRole("button", { name: "Edit" }).click();
        const updatedComment = "Updated e2e comment";
        await commentField.locator("textarea").fill(updatedComment);
        await commentField.getByRole("button", { name: "Save" }).click();

        await expect(commentField.getByText(updatedComment)).toBeVisible();

        // -----------------------------------------------------------------
        // Scenario 4: remove the rating
        // -----------------------------------------------------------------
        await editor.getByRole("button", { name: "Remove" }).click();
        // Confirmation panel -> confirm removal.
        await editor.getByRole("button", { name: "Remove" }).click();

        await expect(editor.getByText("There are no ratings for this assessment")).toBeVisible();
        await expect(editor.getByText(updatedComment)).toHaveCount(0);
    });
});
