import { apiContext } from "./api.js";

/**
 * Bookmark-specific REST seeding helpers.
 *
 * The bookmark create/update/delete endpoints (and the UI edit/remove actions) are guarded by the
 * BOOKMARK_EDITOR role. The out-of-the-box `admin` sample user only has the ADMIN role, so tests
 * must grant BOOKMARK_EDITOR first (roles are resolved server-side per request, so no re-login is
 * needed after granting).
 */

export const BOOKMARK_SECTION_ID = 5; // see waltz-test-common Section enum

/** The embedded, single-section view for an app's bookmarks (isolates the BookmarkPanel). */
export function bookmarksSectionPath(app) {
    return `/embed/internal/APPLICATION/${app.id}/${BOOKMARK_SECTION_ID}`;
}

/**
 * Additively grant roles to a user (POST /api/user/:userName/roles sets the FULL role set, so we
 * union with the user's current roles rather than replacing them). Replacing would clobber roles
 * granted by other specs running in parallel against the shared admin user.
 */
export async function grantRoles(baseURL, token, userName, roles) {
    const ctx = await apiContext(baseURL, token);
    const who = await (await ctx.get("/api/user/whoami")).json();
    const merged = [...new Set([...(who.roles || []), ...roles])];
    const resp = await ctx.post(`/api/user/${userName}/roles`, {
        data: { roles: merged, comment: "waltz-e2e bookmark tests" }
    });
    if (!resp.ok()) {
        throw new Error(`grant roles failed: ${resp.status()} ${await resp.text()}`);
    }
    await ctx.dispose();
}

/** Seed a bookmark against an application via POST /api/bookmarks. Returns the created bookmark. */
export async function seedBookmark(
    baseURL,
    token,
    app,
    { title, url = "http://finos.org", bookmarkKind = "DOCUMENTATION", description } = {}
) {
    const ctx = await apiContext(baseURL, token);
    const resp = await ctx.post("/api/bookmarks", {
        data: {
            parent: { kind: "APPLICATION", id: app.id, name: app.name },
            bookmarkKind,
            title,
            url,
            description,
            lastUpdatedBy: "admin" // required by the model; the server overwrites it
        }
    });
    if (!resp.ok()) {
        throw new Error(`seed bookmark failed: ${resp.status()} ${await resp.text()}`);
    }
    const body = await resp.json();
    await ctx.dispose();
    return body;
}
