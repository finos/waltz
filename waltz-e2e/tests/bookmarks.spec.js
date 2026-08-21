import { test, expect } from "@playwright/test";
import { authenticate, createApp, login, uniqueName } from "./helpers/api.js";
import { bookmarksSectionPath, grantRoles, seedBookmark } from "./helpers/bookmarks.js";

const baseURL = process.env.WALTZ_BASE_URL ?? "http://localhost:8080";

/**
 * Bookmarks e2e (issue #6071 checklist): add, edit, remove and search/filter bookmarks on an
 * application. Setup is via the REST API; the UI is driven only for the behaviour under test.
 *
 * Bookmark mutation (both the REST endpoint and the UI edit/remove/add actions) requires the
 * BOOKMARK_EDITOR role, which the sample `admin` user lacks by default — so each spec grants it.
 * Mirrors the flow in the (bitrotted) Java BookmarkCreationAndRemovalIntegrationTest.
 */

async function openBookmarks(page, context, token, app) {
    await authenticate(context, token);
    await page.goto(bookmarksSectionPath(app));
}

test("adds a bookmark to an application via the UI", async ({ page, context }) => {
    const token = await login(baseURL);
    await grantRoles(baseURL, token, "admin", ["ADMIN", "BOOKMARK_EDITOR"]);
    const app = await createApp(baseURL, token, uniqueName("ts_bm_add_app"));

    await openBookmarks(page, context, token, app);

    // Fresh app => NoData "No bookmarks" with an "Add bookmark" button.
    await page.getByRole("button", { name: "Add bookmark" }).first().click();

    const title = uniqueName("ts_bm_title");
    await page.locator("input#title").fill(title);
    await page.locator("input#url").fill("http://finos.org");
    await page.getByRole("button", { name: "Save" }).click();

    await expect(page.getByRole("link", { name: title })).toBeVisible();
});

test("edits an existing bookmark", async ({ page, context }) => {
    const token = await login(baseURL);
    await grantRoles(baseURL, token, "admin", ["ADMIN", "BOOKMARK_EDITOR"]);
    const app = await createApp(baseURL, token, uniqueName("ts_bm_edit_app"));

    const originalTitle = uniqueName("ts_bm_orig");
    await seedBookmark(baseURL, token, app, { title: originalTitle });

    await openBookmarks(page, context, token, app);

    await expect(page.getByRole("link", { name: originalTitle })).toBeVisible();
    await page.getByRole("button", { name: "Edit" }).click();

    const newTitle = uniqueName("ts_bm_edited");
    const titleInput = page.locator("input#title");
    await expect(titleInput).toHaveValue(originalTitle);
    await titleInput.fill(newTitle);
    await page.getByRole("button", { name: "Save" }).click();

    await expect(page.getByRole("link", { name: newTitle })).toBeVisible();
    await expect(page.getByRole("link", { name: originalTitle })).toHaveCount(0);
});

test("removes a bookmark", async ({ page, context }) => {
    const token = await login(baseURL);
    await grantRoles(baseURL, token, "admin", ["ADMIN", "BOOKMARK_EDITOR"]);
    const app = await createApp(baseURL, token, uniqueName("ts_bm_remove_app"));

    const title = uniqueName("ts_bm_doomed");
    await seedBookmark(baseURL, token, app, { title });

    await openBookmarks(page, context, token, app);

    await expect(page.getByRole("link", { name: title })).toBeVisible();
    await page.getByRole("button", { name: "Remove" }).click();

    // Confirmation panel (BookmarkRemovalConfirmation) — confirm via the warning button.
    await expect(page.getByRole("heading", { name: "Confirm bookmark removal" })).toBeVisible();
    await page.locator("button.btn-warning").click();

    await expect(page.getByRole("link", { name: title })).toHaveCount(0);
    await expect(page.getByText("No bookmarks")).toBeVisible();
});

test("searches/filters the bookmarks list", async ({ page, context }) => {
    const token = await login(baseURL);
    await grantRoles(baseURL, token, "admin", ["ADMIN", "BOOKMARK_EDITOR"]);
    const app = await createApp(baseURL, token, uniqueName("ts_bm_search_app"));

    // The search box only renders when there are more than 5 bookmarks (BookmarkPanel).
    const marker = uniqueName("needle");
    const targetTitle = `${marker}_target`;
    for (const suffix of ["alpha", "bravo", "charlie", "delta", "echo"]) {
        await seedBookmark(baseURL, token, app, { title: uniqueName(`chaff_${suffix}`) });
    }
    await seedBookmark(baseURL, token, app, { title: targetTitle });

    await openBookmarks(page, context, token, app);

    const search = page.locator("input[type=search]");
    await expect(search).toBeVisible();
    await expect(page.getByRole("link", { name: targetTitle })).toBeVisible();

    await search.fill(marker);

    await expect(page.getByRole("link", { name: targetTitle })).toBeVisible();
    // All the chaff bookmarks should be filtered out.
    await expect(page.locator("a", { hasText: "chaff_" })).toHaveCount(0);
});
