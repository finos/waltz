import { test, expect } from "@playwright/test";
import { authenticate, createApp, login, apiContext, uniqueName } from "./helpers/api.js";

const baseURL = process.env.WALTZ_BASE_URL ?? "http://localhost:8080";

/**
 * Involvement e2e (issue #6071 checklist).
 *
 * Flow mirrors the Java InvolvementHelper / SurveyIssuance playwright tests, but:
 *  - Persons have NO create REST endpoint (feed-loaded), so we reuse EXISTING people
 *    discovered via GET /api/person/search/:query (see PersonSearchDao — containsIgnoreCase
 *    on display name, query must be >= 3 chars).
 *  - Involvement kinds are baseline data (GET /api/involvement-kind).
 *  - Editing involvements on an APPLICATION requires the APP_EDITOR role
 *    (see RoleUtilities.getRequiredRoleForApplication); the seeded `admin` user only has
 *    ADMIN, so we additively grant APP_EDITOR via POST /api/user/:userName/roles first.
 *
 * Endpoints used:
 *  - POST /authentication/login
 *  - GET  /api/user/whoami, POST /api/user/:userName/roles           (grant APP_EDITOR)
 *  - GET  /api/person/search/:query                                  (find existing people)
 *  - GET  /api/involvement-kind                                      (baseline kinds)
 *  - POST /api/involvement/entity/APPLICATION/:id  {operation:ADD}   (seed involvements)
 *  - GET  /api/involvement/entity/APPLICATION/:id/people            (verify)
 */

// Common substrings of the generated sample person display-names (>= 3 chars). We probe these
// rather than hard-coding any specific 2023 sample name.
const PERSON_PROBES = [
    "son", "man", "ell", "art", "ing", "ton", "ley", "ber",
    "and", "ren", "lee", "ana", "ard", "erson", "ith"
];

/** Additively grant APP_EDITOR to admin so involvement edits are authorised. */
async function grantAppEditor(token) {
    const ctx = await apiContext(baseURL, token);
    const who = await (await ctx.get("/api/user/whoami")).json();
    const roles = Array.from(new Set([...(who.roles || []), "APP_EDITOR"]));
    const resp = await ctx.post(`/api/user/${who.userName}/roles`, {
        data: { roles, comment: "e2e involvement tests" }
    });
    if (!resp.ok()) {
        throw new Error(`grant roles failed: ${resp.status()} ${await resp.text()}`);
    }
    await ctx.dispose();
}

/** Discover `count` distinct, active people via the search endpoint. */
async function findPeople(token, count) {
    const ctx = await apiContext(baseURL, token);
    const byId = new Map();
    for (const q of PERSON_PROBES) {
        const resp = await ctx.get(`/api/person/search/${q}`);
        if (resp.ok()) {
            for (const p of await resp.json()) {
                if (!p.isRemoved) byId.set(p.id, p);
            }
        }
        if (byId.size >= count) break;
    }
    await ctx.dispose();
    const people = Array.from(byId.values());
    if (people.length < count) {
        throw new Error(`only found ${people.length} people, needed ${count}`);
    }
    return people.slice(0, count);
}

/** First baseline involvement kind valid for applications. */
async function findAppInvolvementKind(token) {
    const ctx = await apiContext(baseURL, token);
    const kinds = await (await ctx.get("/api/involvement-kind")).json();
    await ctx.dispose();
    const kind = kinds.find(k =>
        k.subjectKind === "APPLICATION" && k.userSelectable && !k.permittedRole);
    if (!kind) throw new Error("no baseline APPLICATION involvement kind found");
    return kind;
}

/** Create a fresh user-selectable APPLICATION involvement kind via REST; returns {id, name}. */
async function createAppInvolvementKind(token, name) {
    const ctx = await apiContext(baseURL, token);
    // POST .../update is the create route (see involvement-kind-store.js); ADMIN-gated. A new
    // kind always starts userSelectable=true (the column defaults true; create can't set it).
    const resp = await ctx.post("/api/involvement-kind/update", {
        data: { name, description: name, subjectKind: "APPLICATION", externalId: name, permittedRole: null }
    });
    if (!resp.ok()) {
        throw new Error(`create involvement kind failed: ${resp.status()} ${await resp.text()}`);
    }
    const id = await resp.json();
    await ctx.dispose();
    return { id, name };
}

/** Flip a kind's userSelectable flag to false, mirroring the admin edit PUT (InvolvementKindOverview). */
async function makeKindNonSelectable(token, id) {
    const ctx = await apiContext(baseURL, token);
    const k = await (await ctx.get(`/api/involvement-kind/id/${id}`)).json();
    const same = v => ({ newVal: v, oldVal: v });
    const change = {
        id,
        name: same(k.name),
        description: same(k.description),
        externalId: same(k.externalId),
        userSelectable: { newVal: false, oldVal: k.userSelectable },
        permittedRole: same(k.permittedRole ?? null),
        transitive: same(k.transitive ?? false)
    };
    const resp = await ctx.put("/api/involvement-kind/update", { data: change });
    if (!resp.ok()) {
        throw new Error(`update involvement kind failed: ${resp.status()} ${await resp.text()}`);
    }
    await ctx.dispose();
}

/** Two distinct user-selectable APPLICATION kinds; creates a second if the baseline has only one. */
async function twoAppInvolvementKinds(token) {
    const ctx = await apiContext(baseURL, token);
    const all = await (await ctx.get("/api/involvement-kind")).json();
    await ctx.dispose();
    const selectable = all.filter(k =>
        k.subjectKind === "APPLICATION" && k.userSelectable && !k.permittedRole);
    if (selectable.length >= 2) return selectable.slice(0, 2);
    const created = await createAppInvolvementKind(token, uniqueName("ts_inv_kind"));
    return [selectable[0], created];
}

/** Seed an involvement (person + kind) onto an app via REST. */
async function addInvolvementApi(token, appId, kindId, personId) {
    const ctx = await apiContext(baseURL, token);
    const resp = await ctx.post(`/api/involvement/entity/APPLICATION/${appId}`, {
        data: {
            involvementKindId: kindId,
            personEntityRef: { kind: "PERSON", id: personId },
            operation: "ADD"
        }
    });
    if (!resp.ok()) {
        throw new Error(`add involvement failed: ${resp.status()} ${await resp.text()}`);
    }
    await ctx.dispose();
}

/** Open a named dynamic section from the sidebar and return its root locator. */
async function openPeopleSection(page) {
    await page.locator(".sidebar-expanded button").getByText("People", { exact: true }).first().click();
    const section = page.locator("#people-section");
    await expect(section).toBeVisible();
    return section;
}

/**
 * Enter edit mode for the People section and return the (visible) editor locator.
 * The "Edit" action is a role-gated <span class="btn"> that renders once the
 * section's data has loaded, so wait for it to be visible before clicking.
 */
async function openInvolvementEditor(page, section) {
    const editBtn = section.getByText("Edit", { exact: true });
    await expect(editBtn).toBeVisible();
    await editBtn.click();

    const editor = page.locator("waltz-entity-involvement-editor");
    await expect(editor).toBeVisible();
    return editor;
}

/**
 * Select an existing person through the AngularJS ui-select person picker.
 *
 * The picker is async on two fronts: opening the control reveals a search input
 * (the dropdown is appended to <body>, so it is page- not editor-scoped), and
 * typing triggers a debounced (refresh-delay="300") REST search whose results
 * render as .ui-select-choices-row elements. Each step therefore waits for its
 * precondition (control open, search input visible, matching option rendered)
 * before acting, and the selection is confirmed via the ui-select-match
 * post-condition rather than proceeding blind.
 */
async function selectPerson(page, editor, displayName) {
    const match = editor.locator(".ui-select-match");
    await expect(match).toBeVisible();
    await match.click();

    const search = page.locator("input.ui-select-search");
    await expect(search).toBeVisible();
    await search.fill(displayName);

    const option = page.locator(".ui-select-choices-row").filter({ hasText: displayName }).first();
    await expect(option).toBeVisible();
    await option.click();

    // Post-condition: the picker now shows the chosen person.
    await expect(match).toContainText(displayName);
}

/**
 * Choose the first real involvement kind. The <select>'s options come from an
 * async $q.all([kinds, whoami]) in the editor's $onInit, so wait until a
 * non-blank <option> has rendered before reading/selecting it.
 */
async function selectFirstInvolvementKind(editor) {
    const kindSelect = editor.locator("select");
    await expect
        .poll(async () => (await kindSelect.locator("option").allInnerTexts())
            .some(t => t.trim().length > 0))
        .toBe(true);

    const kindLabel = (await kindSelect.locator("option").allInnerTexts())
        .map(t => t.trim())
        .find(t => t.length > 0);
    await kindSelect.selectOption({ label: kindLabel });
    return kindLabel;
}

let token;
let kind;

test.beforeAll(async () => {
    const bootstrap = await login(baseURL);
    await grantAppEditor(bootstrap);
    // Re-login so the JWT carries the freshly granted APP_EDITOR role.
    token = await login(baseURL);
    kind = await findAppInvolvementKind(token);
});


test("add an involvement (existing person + baseline kind) to an app via the UI", async ({ page, context }) => {
    const [person] = await findPeople(token, 1);
    const app = await createApp(baseURL, token, uniqueName("ts_inv_add"));

    await authenticate(context, token);
    await page.goto(`/application/${app.id}`);

    const section = await openPeopleSection(page);
    const editor = await openInvolvementEditor(page, section);

    // Pick the existing person via the ui-select person search.
    await selectPerson(page, editor, person.displayName);

    // Pick a baseline involvement kind from the dropdown (first real option).
    const kindLabel = await selectFirstInvolvementKind(editor);

    // The Add button is ng-disabled until both person + kind are set; its
    // auto-wait on an enabled control confirms the model is valid before clicking.
    await editor.getByRole("button", { name: "Add" }).click();

    // The new involvement now appears in the editor's current-involvements table.
    const row = editor.locator("tr").filter({ hasText: person.displayName });
    await expect(row).toBeVisible();
    await expect(row).toContainText(kindLabel);
});


test("remove an involvement via the UI", async ({ page, context }) => {
    const [person] = await findPeople(token, 1);
    const app = await createApp(baseURL, token, uniqueName("ts_inv_remove"));
    await addInvolvementApi(token, app.id, kind.id, person.id);

    await authenticate(context, token);
    await page.goto(`/application/${app.id}`);

    const section = await openPeopleSection(page);
    const editor = await openInvolvementEditor(page, section);

    const row = editor.locator("tr").filter({ hasText: person.displayName });
    await expect(row).toBeVisible();

    await row.getByText("Remove").click();

    // After removal the person is gone and the no-data message is shown.
    await expect(editor.locator("tr").filter({ hasText: person.displayName })).toHaveCount(0);
    await expect(editor.getByText("No involvements exist.")).toBeVisible();
});


test("search / filter involvements in the People grid", async ({ page, context }) => {
    // The grid's search box only renders with >= 5 rows, so seed 5 distinct people.
    const people = await findPeople(token, 5);
    const app = await createApp(baseURL, token, uniqueName("ts_inv_search"));
    for (const p of people) {
        await addInvolvementApi(token, app.id, kind.id, p.id);
    }

    await authenticate(context, token);
    await page.goto(`/application/${app.id}`);

    const section = await openPeopleSection(page);

    const target = people[0];
    const searchBox = section.locator(".waltz-search-control input");
    await expect(searchBox).toBeVisible();

    // Filtering to a person's name keeps them visible...
    await searchBox.fill(target.displayName);
    await expect(section.getByText(target.displayName).first()).toBeVisible();

    // ...and a non-matching query filters everyone out.
    await searchBox.fill("zzz_no_such_person_qqq");
    await expect(section.getByText(target.displayName)).toHaveCount(0);
});


test("change (edit) a person's involvement kind via the UI", async ({ page, context }) => {
    // Waltz has no in-place involvement edit (the editor exposes only Add + Remove per person);
    // a person's involvement is "edited" by removing the old kind and adding the new one.
    const [kindA, kindB] = await twoAppInvolvementKinds(token);
    const [person] = await findPeople(token, 1);
    const app = await createApp(baseURL, token, uniqueName("ts_inv_edit"));
    await addInvolvementApi(token, app.id, kindA.id, person.id);

    await authenticate(context, token);
    await page.goto(`/application/${app.id}`);

    const section = await openPeopleSection(page);
    const editor = await openInvolvementEditor(page, section);

    // The seeded involvement shows kind A.
    const rowA = editor.locator("tr").filter({ hasText: person.displayName });
    await expect(rowA).toBeVisible();
    await expect(rowA).toContainText(kindA.name);

    // Remove the kind-A involvement...
    await rowA.getByText("Remove").click();
    await expect(editor.locator("tr").filter({ hasText: person.displayName })).toHaveCount(0);

    // ...then re-add the same person under kind B.
    await selectPerson(page, editor, person.displayName);
    await editor.locator("select").selectOption({ label: kindB.name });
    await editor.getByRole("button", { name: "Add" }).click();

    // The person's involvement is now kind B, not kind A.
    const rowB = editor.locator("tr").filter({ hasText: person.displayName });
    await expect(rowB).toBeVisible();
    await expect(rowB).toContainText(kindB.name);
    await expect(rowB).not.toContainText(kindA.name);
});


test("the involvement editor hides kinds that are not user-selectable", async ({ page, context }) => {
    // The editor's kind picker filters on userSelectable (entity-involvement-editor.js): a kind
    // with userSelectable=false must not be offered, while a user-selectable one must be.
    const hidden = await createAppInvolvementKind(token, uniqueName("ts_inv_hidden"));
    await makeKindNonSelectable(token, hidden.id);

    const app = await createApp(baseURL, token, uniqueName("ts_inv_flag"));

    await authenticate(context, token);
    await page.goto(`/application/${app.id}`);

    const section = await openPeopleSection(page);
    const editor = await openInvolvementEditor(page, section);

    // Wait until the async-loaded options include the known user-selectable baseline kind.
    const kindSelect = editor.locator("select");
    await expect
        .poll(async () => (await kindSelect.locator("option").allInnerTexts())
            .some(t => t.trim() === kind.name))
        .toBe(true);

    const options = (await kindSelect.locator("option").allInnerTexts()).map(t => t.trim());
    expect(options).toContain(kind.name);            // user-selectable kind is offered
    expect(options).not.toContain(hidden.name);      // non-user-selectable kind is filtered out
});
