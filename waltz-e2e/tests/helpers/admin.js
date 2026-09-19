import { request } from "@playwright/test";
import { apiContext } from "./api.js";

/**
 * Admin-area setup helpers, seeding via the same REST endpoints the UI uses.
 * See waltz-web/.../endpoints/api/{UserEndpoint,RoleEndpoint,ActorEndpoint}.java
 */

/** Register a new user via POST /api/user/new-user. */
export async function createUser(baseURL, token, userName, password) {
    const ctx = await apiContext(baseURL, token);
    const resp = await ctx.post("/api/user/new-user", {
        data: { userName, password }
    });
    if (!resp.ok()) {
        throw new Error(`create user failed: ${resp.status()} ${await resp.text()}`);
    }
    await ctx.dispose();
    return { userName, password };
}

/** Set the full role set for a user via POST /api/user/:userName/roles. */
export async function updateRoles(baseURL, token, userName, roles, comment = "e2e") {
    const ctx = await apiContext(baseURL, token);
    const resp = await ctx.post(`/api/user/${encodeURIComponent(userName)}/roles`, {
        data: { roles, comment }
    });
    if (!resp.ok()) {
        throw new Error(`update roles failed: ${resp.status()} ${await resp.text()}`);
    }
    await ctx.dispose();
}

/** Read a user (incl. its roles) via GET /api/user/user-id/:userId. */
export async function getUser(baseURL, token, userName) {
    const ctx = await apiContext(baseURL, token);
    const resp = await ctx.get(`/api/user/user-id/${encodeURIComponent(userName)}`);
    if (!resp.ok()) {
        throw new Error(`get user failed: ${resp.status()} ${await resp.text()}`);
    }
    const body = await resp.json();
    await ctx.dispose();
    return body;
}

/** All roles (system + custom) via GET /api/role. */
export async function getRoles(baseURL, token) {
    const ctx = await apiContext(baseURL, token);
    const resp = await ctx.get("/api/role");
    if (!resp.ok()) {
        throw new Error(`get roles failed: ${resp.status()} ${await resp.text()}`);
    }
    const body = await resp.json();
    await ctx.dispose();
    return body;
}

/** All rating schemes via GET /api/rating-scheme. */
export async function getRatingSchemes(baseURL, token) {
    const ctx = await apiContext(baseURL, token);
    const resp = await ctx.get("/api/rating-scheme");
    if (!resp.ok()) {
        throw new Error(`get rating schemes failed: ${resp.status()} ${await resp.text()}`);
    }
    const body = await resp.json();
    await ctx.dispose();
    return body;
}

/** All assessment definitions via GET /api/assessment-definition. */
export async function getAssessmentDefinitions(baseURL, token) {
    const ctx = await apiContext(baseURL, token);
    const resp = await ctx.get("/api/assessment-definition");
    if (!resp.ok()) {
        throw new Error(`get assessment definitions failed: ${resp.status()} ${await resp.text()}`);
    }
    const body = await resp.json();
    await ctx.dispose();
    return body;
}

/** Create an actor via POST /api/actor/update (requires ACTOR_ADMIN); returns the new id. */
export async function createActor(baseURL, token, name, description = "e2e") {
    const ctx = await apiContext(baseURL, token);
    const resp = await ctx.post("/api/actor/update", {
        data: { name, description, isExternal: false }
    });
    if (!resp.ok()) {
        throw new Error(`create actor failed: ${resp.status()} ${await resp.text()}`);
    }
    const id = await resp.json();
    await ctx.dispose();
    return id;
}

/** All actors via GET /api/actor. */
export async function getActors(baseURL, token) {
    const ctx = await apiContext(baseURL, token);
    const resp = await ctx.get("/api/actor");
    if (!resp.ok()) {
        throw new Error(`get actors failed: ${resp.status()} ${await resp.text()}`);
    }
    const body = await resp.json();
    await ctx.dispose();
    return body;
}

/** Delete an actor via DELETE /api/actor/:id (requires ACTOR_ADMIN); returns the boolean result. */
export async function deleteActor(baseURL, token, id) {
    const ctx = await apiContext(baseURL, token);
    const resp = await ctx.delete(`/api/actor/${id}`);
    if (!resp.ok()) {
        throw new Error(`delete actor failed: ${resp.status()} ${await resp.text()}`);
    }
    const body = await resp.json();
    await ctx.dispose();
    return body;
}

/** All end-user applications (EUDAs) via GET /api/end-user-application. */
export async function getEndUserApps(baseURL, token) {
    const ctx = await apiContext(baseURL, token);
    const resp = await ctx.get("/api/end-user-application");
    if (!resp.ok()) {
        throw new Error(`get end-user-apps failed: ${resp.status()} ${await resp.text()}`);
    }
    const body = await resp.json();
    await ctx.dispose();
    return body;
}

/** Create a static panel via POST /api/static-panel (requires ADMIN). */
export async function createStaticPanel(baseURL, token, panel) {
    const ctx = await apiContext(baseURL, token);
    const resp = await ctx.post("/api/static-panel", { data: panel });
    if (!resp.ok()) {
        throw new Error(`create static panel failed: ${resp.status()} ${await resp.text()}`);
    }
    await ctx.dispose();
}

/** Log in as an arbitrary user (not just admin) and return a JWT bearer token. */
export async function loginAs(baseURL, userName, password) {
    const ctx = await request.newContext({ baseURL });
    const resp = await ctx.post("/authentication/login", {
        data: { userName, password }
    });
    if (!resp.ok()) {
        throw new Error(`login as ${userName} failed: ${resp.status()} ${await resp.text()}`);
    }
    const body = await resp.json();
    await ctx.dispose();
    return body.token;
}
