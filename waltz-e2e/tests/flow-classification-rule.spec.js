import { test, expect } from "@playwright/test";
import { apiContext, authenticate, createApp, login, uniqueName } from "./helpers/api.js";
import { updateRoles } from "./helpers/admin.js";

const baseURL = process.env.WALTZ_BASE_URL ?? "http://localhost:8080";

/**
 * Flow classification rule creation e2e.
 *
 * A flow classification rule categorises data flows and consists of a Subject (the source/target
 * system), a Scope (the set of systems the rule applies to), an optional Data Type and a
 * Classification (e.g. "Primary"). Creating one requires the AUTHORITATIVE_SOURCE_EDITOR role
 * (see FlowClassificationRuleEndpoint#insertRoute), which the seeded `admin` user does not hold,
 * so it is granted additively via the roles API before driving the UI.
 *
 * Setup is seeded via the REST API; the subject app is created with a unique name and the data
 * type / scope / classification are all chosen dynamically from baseline data (never hard-coded).
 *
 * Flow reference: waltz-test-common/.../playwright/flow_classification_rule/RuleCreationTest.java
 * UI reference: waltz-ng/client/flow-classification-rule/components/summary-list/
 *               {FlowClassificationRulesPanel,FlowClassificationRuleEditor}.svelte
 */

/** Read the current user's roles via GET /api/user/whoami. */
async function whoamiRoles(token) {
    const ctx = await apiContext(baseURL, token);
    try {
        return (await (await ctx.get("/api/user/whoami")).json()).roles || [];
    } finally {
        await ctx.dispose();
    }
}

/** GET helper returning parsed JSON. */
async function getJson(token, path, opts) {
    const ctx = await apiContext(baseURL, token);
    try {
        return await (await ctx.get(path, opts)).json();
    } finally {
        await ctx.dispose();
    }
}

test("create a flow classification rule for an application", async ({ page, context }) => {
    let token = await login(baseURL);

    // -- grant the create permission additively, then re-login so the UI sees the new
    // role. LOGICAL_DATA_FLOW_EDITOR is also needed to seed the logical flow (and its
    // data-type decorator) used later to confirm the rating renders on the flow view.
    const roles = await whoamiRoles(token);
    const needed = ["AUTHORITATIVE_SOURCE_EDITOR", "LOGICAL_DATA_FLOW_EDITOR"];
    if (!needed.every(r => roles.includes(r))) {
        await updateRoles(
            baseURL,
            token,
            "admin",
            [...new Set([...roles, ...needed])],
            "e2e: flow classification rule create");
        token = await login(baseURL);
    }

    // -- seed the subject application and pick scope / data type / classification dynamically
    const app = await createApp(baseURL, token, uniqueName("fcr_app"));

    const orgUnits = await getJson(token, "/api/org-unit");
    const scope = orgUnits[0];
    expect(scope, "expected at least one baseline org unit").toBeTruthy();

    const dataTypes = await getJson(token, "/api/data-types");
    const dataType = dataTypes.find(d => d.concrete && !d.unknown);
    expect(dataType, "expected a concrete baseline data type").toBeTruthy();

    const classifications = await getJson(token, "/api/flow-classification");
    const classification = classifications.find(c => c.userSelectable && c.name === "Primary")
        || classifications.find(c => c.userSelectable);
    expect(classification, "expected a user-selectable flow classification").toBeTruthy();

    // -- drive the UI
    await authenticate(context, token);
    await page.goto("/data-types");

    const list = page.getByTestId("flow-classification-rule-list");
    await list.getByTestId("create-rule").click();

    // the Svelte editor form has no testid; scope to the form containing the classification field
    const editor = page.locator("form").filter({ has: page.locator("#classification") });
    await expect(editor).toBeVisible();

    // classification (select drives the visible direction labels). The option only renders once
    // the async flow-classification list has loaded, so wait for it before selecting.
    const classificationSelect = editor.locator("#classification");
    await expect(classificationSelect.locator("option", { hasText: classification.name })).toHaveCount(1);
    await classificationSelect.selectOption({ label: classification.name });

    // subject (simple-svelte-autocomplete over applications). The dropdown is populated by a
    // debounced async search, so wait for the specific option to render before clicking it and
    // confirm the selection registered (the input reflects the chosen name) before moving on.
    const subject = editor.locator("#source");
    await subject.locator("input").fill(app.name);
    await subject.locator(".autocomplete-list-item", { hasText: app.name }).first().click();
    await expect(subject.locator("input")).toHaveValue(app.name);

    // scope (autocomplete over org units etc.) — same deterministic pattern as the subject.
    const scopeField = editor.locator("#scope");
    await scopeField.locator("input").fill(scope.name);
    await scopeField.locator(".autocomplete-list-item", { hasText: scope.name }).first().click();
    await expect(scopeField.locator("input")).toHaveValue(scope.name);

    // data type (tree selector). Typing filters the tree; wait for the matching node button to
    // render, click it, then confirm the selection switched the panel to the "chosen" view (the
    // search input is replaced by the selected data-type label).
    const datatype = editor.locator("#datatype");
    await datatype.locator("input[type=search]").fill(dataType.name);
    const dataTypeNode = datatype.getByRole("button", { name: dataType.name, exact: true }).first();
    await expect(dataTypeNode).toBeVisible();
    await dataTypeNode.click();
    await expect(datatype.locator("input[type=search]")).toHaveCount(0);
    await expect(datatype).toContainText(dataType.name);

    // description is required to enable the submit button
    await editor.locator("#description").fill("Created by e2e flow classification rule test");

    const submit = editor.locator("button[type=submit]");
    await expect(submit).toBeEnabled();
    await submit.click();

    // -- verify: the create panel closes on success (durable signal — the transient "Created rule"
    // toast auto-dismisses after ~3s so must not be asserted), which also refreshes the rule list;
    // then confirm persistence via the API.
    await expect(editor).toBeHidden();

    const allRules = await getJson(token, "/api/flow-classification-rule");
    const created = allRules.find(r => r.subjectReference?.id === app.id
        && r.classificationId === classification.id
        && r.dataTypeId === dataType.id);
    expect(created, "rule should be persisted with the seeded subject/datatype/classification").toBeTruthy();
    expect(created.vantagePointReference.id).toBe(scope.id);

    // open the new rule in the table and confirm the detail panel. Filter the grid to the new
    // rule and wait for its cell to render before clicking it.
    await list.locator("input[type=search]").first().fill(app.name);
    const ruleCell = page.locator(".slick-cell", { hasText: app.name }).first();
    await expect(ruleCell).toBeVisible();
    await ruleCell.click();

    await expect(page.getByTestId("source")).toContainText(app.name);
    await expect(page.getByTestId("data-type")).toContainText(dataType.name);
    await expect(page.getByTestId("scope")).toContainText(scope.name);

    // -- confirm the logical flow view shows the new rating and colour.
    // Seed a logical flow from the subject (the authoritative source) to a target
    // within the rule's scope, carrying the rule's data type, then recalculate flow
    // ratings so the rule is applied to the new flow.
    const target = await createApp(baseURL, token, uniqueName("fcr_flow_tgt"), scope.id);
    const ctx = await apiContext(baseURL, token);
    const lf = await (await ctx.post("/api/logical-flow", {
        data: {
            source: { kind: "APPLICATION", id: app.id },
            target: { kind: "APPLICATION", id: target.id }
        }
    })).json();
    await ctx.post(`/api/data-type-decorator/save/entity/LOGICAL_DATA_FLOW/${lf.id}`, {
        data: {
            entityReference: { kind: "LOGICAL_DATA_FLOW", id: lf.id },
            addedDataTypeIds: [dataType.id],
            removedDataTypeIds: []
        }
    });
    await ctx.get("/api/flow-classification-rule/recalculate-flow-ratings");

    // The rule rates the flow's data type with the chosen classification (API check) ...
    await expect
        .poll(async () => {
            const decorators = await getJson(token, `/api/data-type-decorator/entity/LOGICAL_DATA_FLOW/${lf.id}`);
            return decorators.find(d => d.decoratorEntity.id === dataType.id)?.rating;
        })
        .toBe(classification.code);

    // ... and the logical flow view renders that data type with the classification's
    // colour (the FlowRatingCell source polygon is filled with classification.color).
    await page.goto(`/logical-flow/${lf.id}`);
    const dtCell = page
        .locator("li")
        .filter({ hasText: dataType.name })
        .filter({ has: page.locator("svg polygon") });
    await expect(dtCell.locator("polygon").first()).toHaveAttribute("fill", classification.color);
    await ctx.dispose();
});
