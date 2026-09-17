import { expect } from "@playwright/test";
import { apiContext } from "./api.js";
import { updateRoles } from "./admin.js";

/**
 * Flow-area REST seeding + query helpers (logical flows, physical specifications,
 * physical flows and lineage / flow diagrams).
 *
 * Authoring flows is permission-gated:
 *   - logical flow add/remove and attaching a physical flow resolve to LOGICAL flow
 *     edit rights, granted wholesale by LOGICAL_DATA_FLOW_EDITOR
 *     (see LogicalFlowDao.calculateAmendedFlowOperations);
 *   - a bundled physical specification needs PHYSICAL_SPECIFICATION_EDITOR;
 *   - a lineage (flow diagram) save/delete needs LINEAGE_EDITOR
 *     (see FlowDiagramEndpoint).
 * Role checks read the live USER_ROLE table per request, so API seeding needs no
 * re-login; the UI caches the identity at login, so re-login before driving it.
 *
 * Java references (authoritative payload recipes):
 *   waltz-test-common/.../helpers/{LogicalFlowHelper,PhysicalFlowHelper,PhysicalSpecHelper}.java
 * Endpoints:
 *   waltz-web/.../endpoints/api/{LogicalFlowEndpoint,PhysicalFlowEndpoint,FlowDiagramEndpoint}.java
 */

export const FLOW_ROLES = [
    "LOGICAL_DATA_FLOW_EDITOR",
    "PHYSICAL_SPECIFICATION_EDITOR",
    "LINEAGE_EDITOR"
];

/** An APPLICATION entity reference from a createApp result (or {id, name}). */
export function appRef(app) {
    return { kind: "APPLICATION", id: app.id, name: app.name };
}

/**
 * Grant the flow-authoring roles additively (union with the user's current roles;
 * the roles endpoint replaces the whole set). Caller should re-login afterwards so
 * the UI's cached identity picks up the new roles before it is driven.
 */
export async function grantFlowRoles(baseURL, token, extra = []) {
    const ctx = await apiContext(baseURL, token);
    let roles;
    try {
        const who = await (await ctx.get("/api/user/whoami")).json();
        roles = [...new Set([...(who.roles || []), ...FLOW_ROLES, ...extra])];
    } finally {
        await ctx.dispose();
    }
    await updateRoles(baseURL, token, "admin", roles, "waltz-e2e flow tests");
}

/** Create a logical flow between two entity references via POST /api/logical-flow. */
export async function createLogicalFlow(baseURL, token, source, target) {
    const ctx = await apiContext(baseURL, token);
    try {
        const resp = await ctx.post("/api/logical-flow", { data: { source, target } });
        if (!resp.ok()) {
            throw new Error(`create logical flow failed: ${resp.status()} ${await resp.text()}`);
        }
        return resp.json();
    } finally {
        await ctx.dispose();
    }
}

/**
 * Create a physical flow (and its bundled physical specification) via
 * POST /api/physical-flow. The command embeds a full PhysicalSpecification with a
 * null id, so the server creates the spec and the flow together. Returns
 * {physicalFlowId, specificationId, response}.
 */
export async function createPhysicalFlow(baseURL, token, { logicalFlowId, owningEntityId, name, dataTypeIds = [] }) {
    const ctx = await apiContext(baseURL, token);
    try {
        const resp = await ctx.post("/api/physical-flow", {
            data: {
                specification: {
                    externalId: name,
                    owningEntity: { kind: "APPLICATION", id: owningEntityId },
                    name,
                    description: "waltz-e2e physical flow",
                    format: "UNKNOWN",
                    lastUpdatedBy: "admin",
                    isRemoved: false,
                    created: { by: "admin", at: new Date().toISOString().slice(0, 23) }
                },
                logicalFlowId,
                flowAttributes: {
                    transport: "UNKNOWN",
                    description: "",
                    basisOffset: 1,
                    criticality: "MEDIUM",
                    frequency: "DAILY"
                },
                dataTypeIds
            }
        });
        if (!resp.ok()) {
            throw new Error(`create physical flow failed: ${resp.status()} ${await resp.text()}`);
        }
        const body = await resp.json();
        return {
            physicalFlowId: body.entityReference.id,
            specificationId: body.specificationId,
            response: body
        };
    } finally {
        await ctx.dispose();
    }
}

/** The logical flows touching an application via GET /api/logical-flow/entity/APPLICATION/:id. */
export async function logicalFlowsForApp(ctx, appId) {
    return (await ctx.get(`/api/logical-flow/entity/APPLICATION/${appId}`)).json();
}

/** The physical flows touching an application via GET /api/physical-flow/entity/APPLICATION/:id. */
export async function physicalFlowsForApp(ctx, appId) {
    return (await ctx.get(`/api/physical-flow/entity/APPLICATION/${appId}`)).json();
}

/** The physical flows for a specification via GET /api/physical-flow/specification/:id. */
export async function physicalFlowsForSpec(ctx, specId) {
    return (await ctx.get(`/api/physical-flow/specification/${specId}`)).json();
}

/** The organisational unit an application belongs to (for aggregate-scope views). */
export async function orgUnitIdForApp(ctx, appId) {
    const app = await (await ctx.get(`/api/app/id/${appId}`)).json();
    return app.organisationalUnitId;
}

/** A concrete, known data type from the baseline taxonomy (GET /api/data-types). */
export async function firstConcreteDataType(ctx) {
    const dataTypes = await (await ctx.get("/api/data-types")).json();
    return dataTypes.find(d => d.concrete && !d.unknown);
}

// --- UI helpers: the "Register Physical Flow" wizard ------------------------
//
// When data-flow proposals are disabled (the default) the wizard at
// main.physical-flow.registration is the UI path for creating a logical flow
// (Route step) and then a physical flow (Specification -> Delivery -> Data
// Types -> Create). Each completed step collapses to a read-only summary, so the
// duplicated `#name` inputs across steps are never simultaneously present; the
// EnumSelect selects (#FormatKind / #TransportKind / ...) are unique per page.
// See waltz-ng/client/physical-flows/svelte/{PhysicalFlowRegistrationView,
// LogicalFlowSelectionStep,FlowCreator,PhysicalSpecificationStep,
// PhysicalFlowCharacteristicsStep,DataTypeSelectionStep}.svelte.

/** Open the physical-flow registration wizard for an application (as its source). */
export async function openRegistration(page, appId) {
    await page.goto(`/physical-flow/registration/APPLICATION/${appId}`);
    await expect(page.getByText("Register new Physical Flow").first()).toBeVisible();
}

/** The `.selection-step` block whose StepHeader carries the given label. */
function step(page, label) {
    return page.locator(".selection-step").filter({ hasText: label });
}

/**
 * Route step: create a new downstream logical flow to `targetName` (an app/actor
 * already searchable by name). Leaves the route "Selected".
 */
export async function createDownstreamRoute(page, targetName) {
    const route = step(page, "Route");
    await route.getByRole("button", { name: "create a new downstream" }).click();

    const targetField = route.locator("#target");
    await targetField.locator("input").first().fill(targetName);
    await targetField.locator(".autocomplete-list-item", { hasText: targetName }).first().click();

    await route.getByRole("button", { name: "Create new flow" }).click();
    await expect(route.getByText("Selected Route:")).toBeVisible();
}

/** Specification step: create a new spec with the given name and first format. */
export async function fillSpecification(page, name) {
    const spec = step(page, "Specification");
    await spec.locator("#name").fill(name);
    await spec.locator("#FormatKind").selectOption({ index: 0 });
    await spec.getByRole("button", { name: "Done" }).click();
    await expect(spec.getByText(`Selected Specification: ${name}`)).toBeVisible();
}

/** Delivery Characteristics step: pick the mandatory transport/frequency/criticality. */
export async function fillCharacteristics(page) {
    const chars = step(page, "Delivery Characteristics");
    await chars.locator("#TransportKind").selectOption({ index: 0 });
    await chars.locator("#FrequencyKind").selectOption({ index: 0 });
    await chars.locator("#CriticalityKind").selectOption({ index: 0 });
    await chars.getByRole("button", { name: "Done" }).click();
    await expect(chars.getByText("Selected Characteristics:")).toBeVisible();
}

/** Data Types step: skip (allowed when proposals are disabled). */
export async function skipDataTypes(page) {
    const dt = step(page, "Data Types");
    await dt.getByRole("button", { name: "Skip" }).click();
}

// --- Exports ---------------------------------------------------------------
//
// Both application-scoped and aggregate (org-unit / app-group) flow exports are
// rendered by the "Data Flows" summary dynamic-section (logical-flows-tabgroup-
// section, section id 14); the data-type list export lives on the /data-types
// home page. Each control is a waltz-data-extract-link whose no-format variant
// is a dropdown (Export as csv / xlsx) that downloads client-side via a blob
// anchor (see widgets/data-extract-link + common/file-utils#downloadFile).

/** Dynamic-section id for the "Data Flows" summary (logical + physical export). */
export const FLOW_SUMMARY_SECTION_ID = 14;

/** Dynamic-section id for the "Diagrams" section (Linked Diagrams tab is default). */
export const DIAGRAMS_SECTION_ID = 12;

/** Open an entity's embedded dynamic section (isolates a single panel). */
export async function openSection(page, kind, id, sectionId) {
    await page.goto(`/embed/internal/${kind}/${id}/${sectionId}`);
}

/**
 * Trigger an export from the waltz-data-extract-link with the given visible name
 * and return the resulting Playwright download. The dropdown menu is appended to
 * <body>, so the menu item is matched by visibility rather than by DOM ancestry.
 */
export async function exportViaLink(page, linkName, format = "csv") {
    const link = page.locator("waltz-data-extract-link").filter({ hasText: linkName });
    await link.locator("[uib-dropdown-toggle], a").first().click();
    const download = page.waitForEvent("download");
    await page.getByText(`Export as ${format}`).filter({ visible: true }).click();
    return download;
}

// --- Lineage (flow diagrams) ----------------------------------------------
//
// Waltz models "lineage" as a flow diagram (LINEAGE_EDITOR role, api/flow-diagram).
// Endpoints (see FlowDiagramEndpoint / FlowDiagramEntityEndpoint):
//   POST   api/flow-diagram/entity/:kind/:id   (raw name body) -> new diagram id
//   GET    api/flow-diagram/id/:id
//   GET    api/flow-diagram/entity/:kind/:id
//   POST   api/flow-diagram                    (SaveDiagramCommand) -> diagram id
//   DELETE api/flow-diagram/id/:id
//   GET    api/flow-diagram-entity/entity/:kind/:id

/** Create a (named, persisted) flow diagram for an entity. Returns its id. */
export async function createFlowDiagram(baseURL, token, kind, id, name) {
    const ctx = await apiContext(baseURL, token);
    try {
        const resp = await ctx.post(`/api/flow-diagram/entity/${kind}/${id}`, {
            headers: { "content-type": "text/plain" },
            data: name
        });
        if (!resp.ok()) {
            throw new Error(`create flow diagram failed: ${resp.status()} ${await resp.text()}`);
        }
        return resp.json();
    } finally {
        await ctx.dispose();
    }
}

/**
 * Save a flow diagram (create if `diagramId` omitted) holding the given entities.
 * `entities` are {entityReference:{kind,id}} records (nodes, logical flows and
 * physical-flow decorations, distinguished only by kind). Returns the diagram id.
 */
export async function saveFlowDiagram(baseURL, token, { diagramId, name, description = "", entities = [], positions = {} }) {
    const ctx = await apiContext(baseURL, token);
    try {
        const resp = await ctx.post("/api/flow-diagram", {
            data: {
                diagramId,
                name,
                description,
                entities,
                annotations: [],
                overlays: [],
                layoutData: JSON.stringify({ positions, diagramTransform: "translate(0,0) scale(1)" })
            }
        });
        if (!resp.ok()) {
            throw new Error(`save flow diagram failed: ${resp.status()} ${await resp.text()}`);
        }
        return resp.json();
    } finally {
        await ctx.dispose();
    }
}

/** A flow diagram by id via GET /api/flow-diagram/id/:id. */
export async function getFlowDiagram(ctx, id) {
    return (await ctx.get(`/api/flow-diagram/id/${id}`)).json();
}

/** The flow diagrams referencing an entity via GET /api/flow-diagram/entity/:kind/:id. */
export async function flowDiagramsForEntity(ctx, kind, id) {
    return (await ctx.get(`/api/flow-diagram/entity/${kind}/${id}`)).json();
}

/** The flow-diagram-entity rows referencing an entity (its lineage memberships). */
export async function flowDiagramEntitiesForEntity(ctx, kind, id) {
    return (await ctx.get(`/api/flow-diagram-entity/entity/${kind}/${id}`)).json();
}

// --- UI helpers: the flow-diagram (lineage) editor -------------------------
//
// The editor is a Svelte island mounted at main.flow-diagram.view
// (/flow-diagram/{id}); there are no data-testid attributes. The right-hand
// ContextPanel (div.context-menu) holds the Edit / Clone / Remove controls and,
// in edit mode, the name/description form (#name, #description) with a Save
// (button.btn-success[type=submit]). See flow-diagram/components/diagram-svelte/.

/** Open an existing flow diagram and wait for its editor to mount. */
export async function openDiagram(page, id) {
    await page.goto(`/flow-diagram/${id}`);
    await expect(page.locator(".context-menu")).toBeVisible();
}

/** Enter the diagram's edit mode via the ContextPanel "Edit" button. */
export async function enterDiagramEdit(page) {
    await page.locator(".context-menu").getByRole("button", { name: "Edit" }).click();
}
