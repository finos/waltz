import { request } from "@playwright/test";

/**
 * Test data setup via Waltz's REST API — the same endpoints the UI itself uses.
 * This is the core of the approach: no in-process Java seeding, just HTTP.
 */

const ADMIN_USER = "admin";
const ADMIN_PASSWORD = "password";

/** Authenticate and return a JWT bearer token. */
export async function login(baseURL) {
    const ctx = await request.newContext({ baseURL });
    const resp = await ctx.post("/authentication/login", {
        data: { userName: ADMIN_USER, password: ADMIN_PASSWORD }
    });
    if (!resp.ok()) {
        throw new Error(`login failed: ${resp.status()} ${await resp.text()}`);
    }
    const body = await resp.json();
    await ctx.dispose();
    return body.token;
}

/** An APIRequestContext pre-loaded with the bearer token, for authenticated calls. */
export async function apiContext(baseURL, token) {
    return request.newContext({
        baseURL,
        extraHTTPHeaders: { authorization: `Bearer ${token}` }
    });
}

/** Create an application via POST /api/app (into a baseline org unit). Returns {id, name}. */
export async function createApp(baseURL, token, name, orgUnitId = 10) {
    const ctx = await apiContext(baseURL, token);
    const resp = await ctx.post("/api/app", {
        data: {
            name,
            assetCode: name,
            organisationalUnitId: orgUnitId,
            applicationKind: "IN_HOUSE",
            businessCriticality: "MEDIUM",
            lifecyclePhase: "PRODUCTION",
            overallRating: "G"
        }
    });
    if (!resp.ok()) {
        throw new Error(`create app failed: ${resp.status()} ${await resp.text()}`);
    }
    const body = await resp.json();
    await ctx.dispose();
    return { id: body.id, name };
}

/** Waltz UI reads its JWT from localStorage["satellizer_token"] (see WaltzHttp.js). */
export const TOKEN_STORAGE_KEY = "satellizer_token";

/**
 * Seed the browser session with the JWT so the UI is authenticated, then navigate.
 * Usage: await authenticate(context, token); await page.goto(...).
 */
export async function authenticate(context, token) {
    await context.addInitScript(
        ([key, value]) => localStorage.setItem(key, value),
        [TOKEN_STORAGE_KEY, token]
    );
}

export function uniqueName(prefix) {
    return `${prefix}_${Date.now()}_${Math.floor(Math.random() * 1e6)}`;
}
