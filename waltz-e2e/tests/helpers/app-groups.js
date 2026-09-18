import { apiContext } from "./api.js";

/**
 * App-group setup helpers built on Waltz's REST API — the same endpoints the UI uses.
 * Payloads/paths mirror AppGroupEndpoint.java (POST /api/app-group[/id/:id/...]).
 */

const JSON_HEADERS = { "content-type": "application/json" };

/**
 * Create a group then set its overview (name/description/kind).
 * `POST /api/app-group` seeds an empty PRIVATE group owned by the caller (admin);
 * `POST /api/app-group/id/:id` updates the overview. Returns {id, name, description, kind}.
 */
export async function createAppGroup(baseURL, token, { name, description = "", kind = "PRIVATE" }) {
    const ctx = await apiContext(baseURL, token);

    const createResp = await ctx.post("/api/app-group");
    if (!createResp.ok()) {
        throw new Error(`create app-group failed: ${createResp.status()} ${await createResp.text()}`);
    }
    const id = await createResp.json();

    const updateResp = await ctx.post(`/api/app-group/id/${id}`, {
        data: { id, name, description, appGroupKind: kind }
    });
    if (!updateResp.ok()) {
        throw new Error(`update app-group overview failed: ${updateResp.status()} ${await updateResp.text()}`);
    }

    await ctx.dispose();
    return { id, name, description, kind };
}

/** Add an application to a group. Body is a raw JSON number (readBody(Long.class)). */
export async function addAppToGroup(baseURL, token, groupId, appId) {
    const ctx = await apiContext(baseURL, token);
    const resp = await ctx.post(`/api/app-group/id/${groupId}/applications`, {
        headers: JSON_HEADERS,
        data: JSON.stringify(appId)
    });
    if (!resp.ok()) {
        throw new Error(`add app to group failed: ${resp.status()} ${await resp.text()}`);
    }
    const body = await resp.json();
    await ctx.dispose();
    return body;
}

/** Add an owner (by userId) to a group. Body is the raw userId string (request.body()). */
export async function addOwner(baseURL, token, groupId, userId) {
    const ctx = await apiContext(baseURL, token);
    const resp = await ctx.post(`/api/app-group/id/${groupId}/members/owners`, {
        headers: { "content-type": "text/plain" },
        data: userId
    });
    if (!resp.ok()) {
        throw new Error(`add owner failed: ${resp.status()} ${await resp.text()}`);
    }
    await ctx.dispose();
}

/** Fetch the full group detail (GET /api/app-group/id/:id/detail). */
export async function getGroupDetail(baseURL, token, groupId) {
    const ctx = await apiContext(baseURL, token);
    const resp = await ctx.get(`/api/app-group/id/${groupId}/detail`);
    if (!resp.ok()) {
        throw new Error(`get group detail failed: ${resp.status()} ${await resp.text()}`);
    }
    const body = await resp.json();
    await ctx.dispose();
    return body;
}

/**
 * Remove an owner. The service re-registers the removed user as a plain subscriber/viewer,
 * so this is how we turn the admin (auto-owner on create) into a subscriber for the
 * subscribe/unsubscribe UI flow.
 */
export async function removeOwner(baseURL, token, groupId, ownerId) {
    const ctx = await apiContext(baseURL, token);
    const resp = await ctx.delete(`/api/app-group/id/${groupId}/members/owners/${ownerId}`);
    if (!resp.ok()) {
        throw new Error(`remove owner failed: ${resp.status()} ${await resp.text()}`);
    }
    const body = await resp.json();
    await ctx.dispose();
    return body;
}
