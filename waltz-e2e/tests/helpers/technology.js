import { apiContext } from "./api.js";

/**
 * Technology-area helpers (issue #7573).
 *
 * Servers and databases are asset-inventory entities; a "usage" ties an application (or any
 * entity) to an asset, carrying the environment. These helpers seed both via the REST API:
 *  - assets:  POST /api/server-info/bulk   /  POST /api/database/bulk        (upsert by externalId)
 *  - usages:  POST /api/server-usage/ref/:kind/:id  /  POST /api/database-usage/ref/:kind/:id
 *
 * The write endpoints require the ADMIN role (the e2e admin user has it). Older backends
 * without these endpoints make the seed a no-op (returns null) so the specs can skip cleanly.
 */

/** True when the response indicates the route simply is not deployed on this backend. */
function isRouteMissing(resp) {
    // Unknown /api routes fall through to the SPA/not-found handler (404) rather than JSON.
    return resp.status() === 404 || resp.status() === 405;
}

async function postExpectingJson(ctx, path, data, description) {
    const resp = await ctx.post(path, { data });
    if (isRouteMissing(resp)) {
        return null;
    }
    if (!resp.ok()) {
        throw new Error(`${description} failed: ${resp.status()} ${await resp.text()}`);
    }
    return resp.json();
}

/**
 * Seed one server and one database asset onto an application, each in its own environment.
 * Returns {hostname, databaseName} (the searchable names of the seeded assets) or null when
 * the write endpoints are not available on the target backend.
 */
export async function seedAppTechnology(baseURL, token, appId, suffix) {
    const ctx = await apiContext(baseURL, token);
    try {
        const hostname = `srv_${suffix}`;
        const databaseName = `db_${suffix}`;

        // 1. Server asset + usage link.
        const servers = [{
            hostname,
            externalId: `srv-${suffix}`,
            operatingSystem: "Linux",
            operatingSystemVersion: "5.10",
            location: "London",
            country: "UK",
            lifecycleStatus: "ACTIVE"
        }];
        const savedServers = await postExpectingJson(ctx, "/api/server-info/bulk", servers, "server bulk save");
        if (savedServers === null) {
            return null;
        }
        const serverId = savedServers.find(s => s.hostname === hostname).id;
        await postExpectingJson(
            ctx,
            `/api/server-usage/ref/APPLICATION/${appId}`,
            [{ serverId, environment: "PROD" }],
            "server usage link");

        // 2. Database asset + usage link.
        const databases = [{
            databaseName,
            instanceName: `inst_${suffix}`,
            externalId: `db-${suffix}`,
            dbmsVendor: "PostgreSQL",
            dbmsName: "PostgreSQL",
            dbmsVersion: "15",
            lifecycleStatus: "ACTIVE"
        }];
        const savedDatabases = await postExpectingJson(ctx, "/api/database/bulk", databases, "database bulk save");
        if (savedDatabases === null) {
            return null;
        }
        const databaseId = savedDatabases.find(d => d.databaseName === databaseName).id;
        await postExpectingJson(
            ctx,
            `/api/database-usage/ref/APPLICATION/${appId}`,
            [{ databaseId, environment: "QA" }],
            "database usage link");

        return { hostname, databaseName };
    } finally {
        await ctx.dispose();
    }
}
