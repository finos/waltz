import { apiContext } from "./api.js";

/**
 * Entity-relationship setup helpers. Relationships in the specific-context editors (app groups,
 * change initiatives, ...) are `RELATES_TO` links stored via /api/entity-relationship. These
 * endpoints apply no role check, and creating an app group makes the caller its owner, so the
 * "Groups" editor is editable without granting extra roles.
 * See waltz-web/.../endpoints/api/{EntityRelationshipEndpoint,AppGroupEndpoint}.java and the
 * data-extract EntityRelationshipsExtractor.
 */

/**
 * Create an app group via POST /api/app-group, then rename it via POST /api/app-group/id/:id so it
 * has a unique, searchable name (the create endpoint always names it "New group created by: ...").
 */
export async function createAppGroup(baseURL, token, name) {
    const ctx = await apiContext(baseURL, token);
    const created = await ctx.post("/api/app-group");
    if (!created.ok()) {
        throw new Error(`create app group failed: ${created.status()} ${await created.text()}`);
    }
    const id = await created.json();
    const renamed = await ctx.post(`/api/app-group/id/${id}`, {
        data: { id, name, description: "created by e2e", appGroupKind: "PRIVATE" }
    });
    if (!renamed.ok()) {
        throw new Error(`rename app group failed: ${renamed.status()} ${await renamed.text()}`);
    }
    await ctx.dispose();
    return { id, name };
}

/** All change initiatives via GET /api/change-initiative/all. */
export async function getChangeInitiatives(baseURL, token) {
    const ctx = await apiContext(baseURL, token);
    const resp = await ctx.get("/api/change-initiative/all");
    if (!resp.ok()) {
        throw new Error(`get change initiatives failed: ${resp.status()} ${await resp.text()}`);
    }
    const body = await resp.json();
    await ctx.dispose();
    return body;
}

/** Relationships involving an entity via GET /api/entity-relationship/entity/:kind/:id. */
export async function getEntityRelationships(baseURL, token, kind, id) {
    const ctx = await apiContext(baseURL, token);
    const resp = await ctx.get(`/api/entity-relationship/entity/${kind}/${id}`);
    if (!resp.ok()) {
        throw new Error(`get entity relationships failed: ${resp.status()} ${await resp.text()}`);
    }
    const body = await resp.json();
    await ctx.dispose();
    return body;
}

/** Create a relationship via POST /api/entity-relationship/relationship/:aKind/:aId/:bKind/:bId/:code. */
export async function createEntityRelationship(baseURL, token, aKind, aId, bKind, bId, code = "RELATES_TO") {
    const ctx = await apiContext(baseURL, token);
    const resp = await ctx.post(
        `/api/entity-relationship/relationship/${aKind}/${aId}/${bKind}/${bId}/${code}`);
    if (!resp.ok()) {
        throw new Error(`create relationship failed: ${resp.status()} ${await resp.text()}`);
    }
    await ctx.dispose();
}

/** Export an entity's relationships via GET /data-extract/entity-relationships/kind/:kind/id/:id. */
export async function exportEntityRelationships(baseURL, token, kind, id, format = "CSV") {
    const ctx = await apiContext(baseURL, token);
    const resp = await ctx.get(`/data-extract/entity-relationships/kind/${kind}/id/${id}?format=${format}`);
    if (!resp.ok()) {
        throw new Error(`export relationships failed: ${resp.status()} ${await resp.text()}`);
    }
    const body = await resp.text();
    await ctx.dispose();
    return body;
}
