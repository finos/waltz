import { test, expect } from "@playwright/test";
import { apiContext, authenticate, createApp, login, uniqueName } from "./helpers/api.js";
import * as flow from "./helpers/flow.js";

const baseURL = process.env.WALTZ_BASE_URL ?? "http://localhost:8080";

/**
 * Flows area e2e (issue #7574): logical flows, physical flows and lineage.
 *
 * Setup is seeded via the REST API (apps, and — where a scenario needs an
 * existing flow — logical/physical flows too); the AngularJS/Svelte UI is then
 * driven. Authoring flows needs edit roles the seeded `admin` user lacks, so
 * flow.grantFlowRoles grants them additively before each test re-logs in so the
 * UI's cached identity sees them.
 *
 * Java references: waltz-test-common/.../helpers/{LogicalFlow,PhysicalFlow,
 * PhysicalSpec}Helper.java. Endpoints under waltz-web/.../endpoints/api/.
 */

/** Grant the flow-authoring roles and return a fresh token that carries them. */
async function loginWithFlowRoles() {
    let token = await login(baseURL);
    await flow.grantFlowRoles(baseURL, token);
    return login(baseURL);
}

test("create a logical flow between two applications", async ({ page, context }) => {
    const token = await loginWithFlowRoles();
    const source = await createApp(baseURL, token, uniqueName("lf_src"));
    const target = await createApp(baseURL, token, uniqueName("lf_tgt"));

    await authenticate(context, token);
    await flow.openRegistration(page, source.id);

    // The Route step is the UI path for creating a logical flow (proposals off).
    await flow.createDownstreamRoute(page, target.name);

    // Verify persistence via the API: a logical flow now runs source -> target.
    const ctx = await apiContext(baseURL, token);
    await expect
        .poll(async () => {
            const flows = await flow.logicalFlowsForApp(ctx, source.id);
            return flows.some(f => f.source.id === source.id && f.target.id === target.id);
        })
        .toBe(true);
    await ctx.dispose();
});

test("register a physical flow on a new logical flow", async ({ page, context }) => {
    const token = await loginWithFlowRoles();
    const source = await createApp(baseURL, token, uniqueName("pf_src"));
    const target = await createApp(baseURL, token, uniqueName("pf_tgt"));
    const specName = uniqueName("pf_spec");

    await authenticate(context, token);
    await flow.openRegistration(page, source.id);

    // Walk the wizard: create the route, the spec, the delivery characteristics,
    // skip data types, then submit.
    await flow.createDownstreamRoute(page, target.name);
    await flow.fillSpecification(page, specName);
    await flow.fillCharacteristics(page);
    await flow.skipDataTypes(page);
    await page.getByRole("button", { name: "Create", exact: true }).click();

    // On success the wizard navigates to the physical-flow view page, which shows
    // the specification we named.
    await page.waitForURL("**/physical-flow/*");
    await expect(page.getByText(specName).first()).toBeVisible();

    // Verify persistence via the API: the source app now has a physical flow, and
    // its specification carries the name entered in the wizard.
    const ctx = await apiContext(baseURL, token);
    const flows = await flow.physicalFlowsForApp(ctx, source.id);
    expect(flows.length).toBeGreaterThan(0);
    const specs = await Promise.all(
        flows.map(f => ctx.get(`/api/physical-specification/id/${f.specificationId}`).then(r => r.json()))
    );
    expect(specs.some(s => s.name === specName)).toBe(true);
    await ctx.dispose();
});

/**
 * Seed a source app with an outgoing logical + physical flow, so the flow-summary
 * section has data to export. `orgUnitId` is where createApp places the app (10).
 */
async function seedAppWithFlows(token, prefix) {
    const source = await createApp(baseURL, token, uniqueName(`${prefix}_src`));
    const target = await createApp(baseURL, token, uniqueName(`${prefix}_tgt`));
    const lf = await flow.createLogicalFlow(baseURL, token, flow.appRef(source), flow.appRef(target));
    const pf = await flow.createPhysicalFlow(baseURL, token, {
        logicalFlowId: lf.id,
        owningEntityId: source.id,
        name: uniqueName(`${prefix}_spec`)
    });
    return { source, target, logicalFlowId: lf.id, ...pf };
}

test("export logical flows at application and aggregate scope", async ({ page, context }) => {
    const token = await loginWithFlowRoles();
    const { source } = await seedAppWithFlows(token, "exp_lf");
    const ctx = await apiContext(baseURL, token);
    const orgUnitId = await flow.orgUnitIdForApp(ctx, source.id);

    await authenticate(context, token);

    // Application scope: the app's Data Flows summary section.
    await flow.openSection(page, "APPLICATION", source.id, flow.FLOW_SUMMARY_SECTION_ID);
    const appCsv = await flow.exportViaLink(page, "Export Logical Flows", "csv");
    expect(appCsv.suggestedFilename()).toMatch(/\.csv$/);

    const appXlsx = await flow.exportViaLink(page, "Export Logical Flows", "xlsx");
    expect(appXlsx.suggestedFilename()).toMatch(/\.xlsx$/);

    // Aggregate scope: the org unit the seeded app belongs to.
    await flow.openSection(page, "ORG_UNIT", orgUnitId, flow.FLOW_SUMMARY_SECTION_ID);
    const aggCsv = await flow.exportViaLink(page, "Export Logical Flows", "csv");
    expect(aggCsv.suggestedFilename()).toMatch(/\.csv$/);

    const aggXlsx = await flow.exportViaLink(page, "Export Logical Flows", "xlsx");
    expect(aggXlsx.suggestedFilename()).toMatch(/\.xlsx$/);
    await ctx.dispose();
});

test("export physical flows at application and aggregate scope", async ({ page, context }) => {
    const token = await loginWithFlowRoles();
    const { source } = await seedAppWithFlows(token, "exp_pf");
    const ctx = await apiContext(baseURL, token);
    const orgUnitId = await flow.orgUnitIdForApp(ctx, source.id);

    await authenticate(context, token);

    // Application scope.
    await flow.openSection(page, "APPLICATION", source.id, flow.FLOW_SUMMARY_SECTION_ID);
    const appCsv = await flow.exportViaLink(page, "Export Physical Flows", "csv");
    expect(appCsv.suggestedFilename()).toMatch(/\.csv$/);

    const appXlsx = await flow.exportViaLink(page, "Export Physical Flows", "xlsx");
    expect(appXlsx.suggestedFilename()).toMatch(/\.xlsx$/);

    // Aggregate scope: the org unit the seeded app belongs to.
    await flow.openSection(page, "ORG_UNIT", orgUnitId, flow.FLOW_SUMMARY_SECTION_ID);
    const aggCsv = await flow.exportViaLink(page, "Export Physical Flows", "csv");
    expect(aggCsv.suggestedFilename()).toMatch(/\.csv$/);

    const aggXlsx = await flow.exportViaLink(page, "Export Physical Flows", "xlsx");
    expect(aggXlsx.suggestedFilename()).toMatch(/\.xlsx$/);
    await ctx.dispose();
});

test("export the data type list as csv and xlsx", async ({ page, context }) => {
    const token = await login(baseURL);

    await authenticate(context, token);
    await page.goto("/data-types");
    await expect(page.locator("#data-types-tree-section")).toBeVisible();

    const csv = await flow.exportViaLink(page, "Export", "csv");
    expect(csv.suggestedFilename()).toBe("data-types.csv");

    const xlsx = await flow.exportViaLink(page, "Export", "xlsx");
    expect(xlsx.suggestedFilename()).toBe("data-types.xlsx");
});

/**
 * The data type list export (waltz-data-extract-link on /data-types) offers only
 * csv and xlsx. SVG export exists in Waltz only for rendered SVG diagram
 * visualisations (waltz-svg-diagram, format="SVG"), not for the data type list,
 * so the "svg" part of the checklist item has no control to drive in this build.
 */
test.fixme("export the data type list as svg", async () => {
    // Blocked: no SVG export control on the data type list page; only csv/xlsx are
    // offered. Enable if/when the data type list gains an SVG (tree) export.
});

/**
 * Deleting a logical flow that still has physical flows is NOT prevented on this
 * build: LogicalFlowService.removeFlow cascades (the view warns "any physical
 * flows that are attached to the flow will also be removed"). The prevent-delete
 * behaviour lives on the unmerged branch waltz-1762-prevent-log-flow-delete-if-
 * physical, so there is nothing to assert here yet.
 */
test.fixme("prevent deletion of a logical flow that has physical flows", async () => {
    // Blocked: master cascades physical-flow removal instead of blocking the
    // logical-flow delete. Enable once the prevent-delete guard (waltz-1762) lands.
});

/**
 * Deleting a physical flow that is used in a lineage is NOT prevented on this
 * build: the physical-flow view's guard (ctrl.mentions -> disabled Delete +
 * "cannot be deleted as it is being used in a lineage" popover) is dead — the
 * lineage-contribution loader that populated `mentions` was removed, so it is
 * always undefined and never disables the button. PhysicalFlowService.delete has
 * no lineage check either, so the delete simply proceeds.
 */
test.fixme("prevent deletion of a physical flow used in a lineage", async () => {
    // Blocked: the used-in-a-lineage delete guard is dead code (mentions is never
    // populated) and unenforced server-side. Enable when the guard is restored.
});

test("create a lineage (flow diagram) for an application", async ({ page, context }) => {
    const token = await loginWithFlowRoles();
    const app = await createApp(baseURL, token, uniqueName("lin_app"));
    const name = uniqueName("lineage");

    await authenticate(context, token);
    // "Create new flow diagram" prompts for the name via a native dialog.
    page.once("dialog", d => d.accept(name));
    await flow.openSection(page, "APPLICATION", app.id, flow.DIAGRAMS_SECTION_ID);
    await page.getByText("Create new flow diagram").click();

    // On success the app navigates to the new diagram; verify via the API.
    await page.waitForURL("**/flow-diagram/*");
    const ctx = await apiContext(baseURL, token);
    await expect
        .poll(async () => {
            const diagrams = await flow.flowDiagramsForEntity(ctx, "APPLICATION", app.id);
            return diagrams.some(d => d.name === name);
        })
        .toBe(true);
    await ctx.dispose();
});

test("edit a lineage's name", async ({ page, context }) => {
    const token = await loginWithFlowRoles();
    const app = await createApp(baseURL, token, uniqueName("lin_edit_app"));
    const diagramId = await flow.createFlowDiagram(baseURL, token, "APPLICATION", app.id, uniqueName("lineage"));
    const newName = uniqueName("renamed");

    await authenticate(context, token);
    await flow.openDiagram(page, diagramId);
    await flow.enterDiagramEdit(page);

    await page.locator("#name").fill(newName);
    await page.locator(".context-menu").getByRole("button", { name: "Save" }).click();

    const ctx = await apiContext(baseURL, token);
    await expect.poll(async () => (await flow.getFlowDiagram(ctx, diagramId)).name).toBe(newName);
    await ctx.dispose();
});

test("add a physical flow to an existing lineage", async ({ page, context }) => {
    const token = await loginWithFlowRoles();
    const { source, target, logicalFlowId, physicalFlowId } = await seedAppWithFlows(token, "lin_add");

    // Seed a diagram holding both app nodes and the logical flow (so the flow
    // bucket renders on the canvas), but NOT the physical flow decoration yet.
    const diagramId = await flow.saveFlowDiagram(baseURL, token, {
        name: uniqueName("lineage"),
        entities: [
            { entityReference: { kind: "APPLICATION", id: source.id }, isNotable: false },
            { entityReference: { kind: "APPLICATION", id: target.id }, isNotable: false },
            { entityReference: { kind: "LOGICAL_DATA_FLOW", id: logicalFlowId } }
        ],
        positions: {
            [`APPLICATION/${source.id}`]: { x: 200, y: 150 },
            [`APPLICATION/${target.id}`]: { x: 600, y: 150 }
        }
    });

    await authenticate(context, token);
    await flow.openDiagram(page, diagramId);

    // Select the logical flow on the canvas (stays in VIEW mode so the Context tab,
    // and hence the FlowPanel, is shown). The FlowPanel's own Edit -> Edit physical
    // flows opens the picker; the header Edit (overview) is deliberately avoided.
    await page.locator(`g[data-flow-id="LOGICAL_DATA_FLOW/${logicalFlowId}"] .wfd-flow-bucket`).click();
    const flowPanel = page.locator(".context-menu .wt-tab.wt-active");
    await flowPanel.getByRole("button", { name: "Edit" }).click();
    await flowPanel.getByRole("button", { name: "Edit physical flows" }).click();
    await flowPanel.locator("input[type=checkbox]").first().check();
    await flowPanel.getByRole("button", { name: "Update flows" }).click();

    // Persist the canvas changes (the inline "save" appears once the diagram is dirty).
    await page.locator(".context-menu").getByRole("button", { name: "save" }).click();

    // Verify: the physical flow is now referenced by a flow-diagram entity (lineage).
    const ctx = await apiContext(baseURL, token);
    await expect
        .poll(async () => {
            const rows = await flow.flowDiagramEntitiesForEntity(ctx, "PHYSICAL_FLOW", physicalFlowId);
            return rows.length;
        })
        .toBeGreaterThan(0);
    await ctx.dispose();
});

test("delete a lineage", async ({ page, context }) => {
    const token = await loginWithFlowRoles();
    const app = await createApp(baseURL, token, uniqueName("lin_del_app"));
    const diagramId = await flow.createFlowDiagram(baseURL, token, "APPLICATION", app.id, uniqueName("lineage"));

    await authenticate(context, token);
    await flow.openDiagram(page, diagramId);

    // Remove (context panel) -> confirm in the in-panel prompt (danger button).
    await page.locator(".context-menu").getByRole("button", { name: "Remove" }).click();
    await page.locator(".context-menu").locator("button.btn-danger", { hasText: "Remove" }).click();

    const ctx = await apiContext(baseURL, token);
    await expect
        .poll(async () => {
            const diagrams = await flow.flowDiagramsForEntity(ctx, "APPLICATION", app.id);
            return diagrams.some(d => d.id === diagramId);
        })
        .toBe(false);
    await ctx.dispose();
});
