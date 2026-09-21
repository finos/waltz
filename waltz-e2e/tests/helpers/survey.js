import { expect } from "@playwright/test";
import { apiContext } from "./api.js";

/**
 * Survey seeding + UI helpers.
 *
 * Surveys are the deepest area to seed: a completable instance needs a template
 * with questions, an active status, a run bound to a selector + involvement
 * kind, and a recipient Person. There is no create-person endpoint, but the
 * `admin` login is itself a feed-loaded Person, so we make admin the recipient
 * (via an involvement) and drive the whole lifecycle as that person. See the
 * README "Known seeding gaps".
 */

/**
 * The `admin` login maps to a real Person record. Survey recipients/owners are
 * Persons, so look it up rather than hardcoding the sample id.
 */
export async function getAdminPerson(ctx) {
    const resp = await ctx.get("/api/person/search/admin");
    const people = await resp.json();
    const person = people.find(p => p.userId === "admin" || p.email === "admin") ?? people[0];
    if (!person) {
        throw new Error("could not resolve the 'admin' person record");
    }
    return person;
}

/**
 * Find any active Person other than `excludeId`, for reassignment scenarios.
 * Person search matches on name substrings, so try a few common fragments.
 */
export async function findOtherPerson(ctx, excludeId) {
    for (const fragment of ["smi", "ell", "ann", "art", "son", "ley", "ric", "and"]) {
        const resp = await ctx.get(`/api/person/search/${fragment}`);
        if (!resp.ok()) {
            continue;
        }
        const people = await resp.json();
        const person = people.find(p => p.id !== excludeId && p.userId !== "admin");
        if (person) {
            return person;
        }
    }
    throw new Error("could not find an alternative person via search");
}

/**
 * An involvement kind whose subject is APPLICATION — needed both to link admin
 * to an app and to appear in the run-create wizard's kind picker (it filters to
 * the template's target kind). Returns the full kind {id, name, subjectKind}.
 */
export async function applicationInvolvementKind(ctx) {
    const resp = await ctx.get("/api/involvement-kind");
    const kinds = await resp.json();
    const kind = kinds.find(k => k.subjectKind === "APPLICATION") ?? kinds[0];
    return kind;
}

/** Link a person to an application via an involvement kind. */
export async function addAppInvolvement(ctx, appId, personId, involvementKindId) {
    const resp = await ctx.post(`/api/involvement/entity/APPLICATION/${appId}`, {
        data: {
            operation: "ADD",
            personEntityRef: { kind: "PERSON", id: personId },
            involvementKindId
        }
    });
    if (!resp.ok()) {
        throw new Error(`add involvement failed: ${resp.status()} ${await resp.text()}`);
    }
}

/**
 * Create a public app group with a searchable name. `POST /api/app-group`
 * ignores its body (it always makes a default-named private group), so rename
 * it via the overview endpoint and make it public so entity search finds it.
 * Returns {id, name}.
 */
export async function createAppGroup(ctx, name) {
    const created = await ctx.post("/api/app-group", { data: {} });
    if (!created.ok()) {
        throw new Error(`create app group failed: ${created.status()} ${await created.text()}`);
    }
    const id = (await created.json());
    const groupId = id.id ?? id;
    const renamed = await ctx.post(`/api/app-group/id/${groupId}`, {
        data: { id: groupId, name, description: name, appGroupKind: "PUBLIC" }
    });
    if (!renamed.ok()) {
        throw new Error(`rename app group failed: ${renamed.status()} ${await renamed.text()}`);
    }
    return { id: groupId, name };
}

/** Add an application to an app group (body is a raw application id). */
export async function addAppToGroup(ctx, groupId, appId) {
    const resp = await ctx.post(`/api/app-group/id/${groupId}/applications`, { data: appId });
    if (!resp.ok()) {
        throw new Error(`add app to group failed: ${resp.status()} ${await resp.text()}`);
    }
}

/** Create a survey template. Returns its id. */
export async function createTemplate(ctx, { name, externalId, targetEntityKind = "APPLICATION" }) {
    const resp = await ctx.post("/api/survey-template", {
        data: { name, description: name, targetEntityKind, externalId, issuanceRole: null }
    });
    if (!resp.ok()) {
        throw new Error(`create template failed: ${resp.status()} ${await resp.text()}`);
    }
    return resp.json();
}

/** Add a question to a template. Returns its id. */
export async function addQuestion(ctx, {
    templateId,
    questionText,
    externalId,
    fieldType = "BOOLEAN",
    isMandatory = false,
    position = 1,
    inclusionPredicate = null,
    allowComment = true
}) {
    const question = { surveyTemplateId: templateId, questionText, externalId, fieldType, isMandatory, position, allowComment };
    if (inclusionPredicate) {
        question.inclusionPredicate = inclusionPredicate;
    }
    const resp = await ctx.post("/api/survey-question", { data: { question, dropdownEntries: [] } });
    if (!resp.ok()) {
        throw new Error(`add question failed: ${resp.status()} ${await resp.text()}`);
    }
    return resp.json();
}

/** Move a template to a release status (DRAFT / ACTIVE / OBSOLETE). */
export async function setTemplateStatus(ctx, templateId, newStatus) {
    const resp = await ctx.put(`/api/survey-template/${templateId}/status`, { data: { newStatus } });
    if (!resp.ok()) {
        throw new Error(`template status change failed: ${resp.status()} ${await resp.text()}`);
    }
}

/** Create a survey run against an application selector. Returns the run id. */
export async function createRun(ctx, {
    templateId,
    selectorRef,
    involvementKindIds = [],
    ownerInvKindIds = [],
    issuanceKind = "INDIVIDUAL",
    name = "e2e survey run",
    dueDate = "2030-01-01",
    approvalDueDate = "2030-02-01",
    contactEmail = "e2e@waltz.test"
}) {
    const resp = await ctx.post("/api/survey-run", {
        data: {
            name,
            description: name,
            surveyTemplateId: templateId,
            selectionOptions: { entityReference: selectorRef, scope: "EXACT" },
            involvementKindIds,
            ownerInvKindIds,
            dueDate,
            approvalDueDate,
            issuanceKind,
            contactEmail
        }
    });
    if (!resp.ok()) {
        throw new Error(`create run failed: ${resp.status()} ${await resp.text()}`);
    }
    const body = await resp.json();
    return body.id ?? body;
}

/** Create instances for explicit people and issue the run. */
export async function issueInstancesTo(ctx, runId, recipientPersonIds, ownerPersonIds = []) {
    const create = await ctx.post(`/api/survey-run/${runId}/create-instances`, {
        data: { recipientPersonIds, ownerPersonIds, owningRole: null }
    });
    if (!create.ok()) {
        throw new Error(`create instances failed: ${create.status()} ${await create.text()}`);
    }
    const issue = await ctx.put(`/api/survey-run/${runId}/status`, { data: { newStatus: "ISSUED" } });
    if (!issue.ok()) {
        throw new Error(`issue run failed: ${issue.status()} ${await issue.text()}`);
    }
}

/** The instances belonging to a run. */
export async function instancesForRun(ctx, runId) {
    const resp = await ctx.get(`/api/survey-instance/run/${runId}`);
    if (!resp.ok()) {
        throw new Error(`list instances failed: ${resp.status()} ${await resp.text()}`);
    }
    return resp.json();
}

/** Current status of an instance. */
export async function instanceStatus(ctx, instanceId) {
    const resp = await ctx.get(`/api/survey-instance/id/${instanceId}`);
    if (!resp.ok()) {
        throw new Error(`get instance failed: ${resp.status()} ${await resp.text()}`);
    }
    const body = await resp.json();
    return body.status;
}

/** Drive an instance status transition via REST (for seeding a start state). */
export async function changeStatus(ctx, instanceId, action, reason = "e2e") {
    const resp = await ctx.put(`/api/survey-instance/${instanceId}/status`, { data: { action, reason } });
    if (!resp.ok()) {
        throw new Error(`status change (${action}) failed: ${resp.status()} ${await resp.text()}`);
    }
}

/**
 * Grant the survey roles the tests need to the shared admin user, preserving
 * any existing roles (never REPLACE — that would race other specs).
 */
export async function grantSurveyRoles(ctx) {
    const who = await (await ctx.get("/api/user/whoami")).json();
    const roles = new Set([...(who.roles ?? []), "SURVEY_TEMPLATE_ADMIN", "SURVEY_ADMIN"]);
    const resp = await ctx.post(`/api/user/${who.userName}/roles`, {
        data: { roles: [...roles], comment: "waltz-e2e survey tests" }
    });
    if (!resp.ok()) {
        throw new Error(`grant roles failed: ${resp.status()} ${await resp.text()}`);
    }
}

/**
 * Seed a fully-issued survey instance whose recipient is the admin person, so
 * the browser session (logged in as admin) can drive the response lifecycle.
 *
 * Returns { runId, instanceId, appId, appName, groupId, groupName, adminPersonId,
 *           involvementKindId, templateId }.
 */
export async function seedIssuedSurveyForAdmin(baseURL, token, {
    createApp,
    uniqueName,
    questions = [{ questionText: "In scope?", fieldType: "BOOLEAN" }],
    issuanceKind = "INDIVIDUAL"
}) {
    const ctx = await apiContext(baseURL, token);
    try {
        const admin = await getAdminPerson(ctx);
        const involvementKind = await applicationInvolvementKind(ctx);
        const involvementKindId = involvementKind.id;

        const app = await createApp(baseURL, token, uniqueName("ts_survey_app"));
        await addAppInvolvement(ctx, app.id, admin.id, involvementKindId);

        const templateId = await createTemplate(ctx, {
            name: uniqueName("ts_survey_tmpl"),
            externalId: uniqueName("TS_TMPL")
        });
        let position = 1;
        for (const q of questions) {
            await addQuestion(ctx, {
                templateId,
                position: position++,
                externalId: uniqueName("TS_Q"),
                ...q
            });
        }
        await setTemplateStatus(ctx, templateId, "ACTIVE");

        const runId = await createRun(ctx, {
            templateId,
            selectorRef: { kind: "APPLICATION", id: app.id },
            involvementKindIds: [involvementKindId],
            issuanceKind,
            name: uniqueName("ts_survey_run")
        });
        await issueInstancesTo(ctx, runId, [admin.id]);

        const instances = await instancesForRun(ctx, runId);
        expect(instances.length).toBeGreaterThan(0);

        return {
            runId,
            instanceId: instances[0].id,
            appId: app.id,
            appName: app.name,
            adminPersonId: admin.id,
            adminPersonName: admin.displayName,
            involvementKind,
            involvementKindId,
            templateId
        };
    } finally {
        await ctx.dispose();
    }
}

/**
 * Drive a survey lifecycle action on the Svelte response-view actions panel.
 *
 * Actions render as buttons labelled by the action display (Submit / Approve /
 * Reject / Withdraw / Reopen). Confirm-and-comment actions open a form with a
 * reason textarea; NOT_REQUIRED actions (Reopen) fire immediately.
 */
export async function invokeSurveyAction(page, label, { needsReason = true } = {}) {
    const actions = page.locator("div.actions");
    await actions.getByRole("button", { name: label }).click();

    if (needsReason) {
        const form = page.locator("form:has(textarea.form-control)");
        await expect(form.locator("textarea.form-control")).toBeVisible();
        await form.locator("textarea.form-control").fill(`e2e ${label.toLowerCase()}`);
        await form.getByRole("button", { name: label }).click();
    }
}

/**
 * Pick an option from a Waltz AngularJS ui-select (waltz-entity-selector). Open
 * the match, type into the currently-visible search box, then click the choice
 * row containing the token.
 */
export async function pickInUiSelect(scope, page, query, token) {
    await scope.locator(".ui-select-match").click();
    const search = page.locator("input.ui-select-search:visible");
    await search.fill(query);
    await page.locator(".ui-select-choices-row", { hasText: token }).first().click();
}

/**
 * Add a person through a Svelte PersonList (used for recipients/owners).
 * `row` scopes to the containing table row so recipient vs owner is unambiguous.
 */
export async function addPersonViaPersonList(row, page, query, personName) {
    await row.getByRole("button", { name: "Add additional person" }).click();
    const input = row.locator("input.autocomplete-input");
    await input.fill(query);
    const choice = page.locator(".autocomplete-list-item", { hasText: personName });
    await choice.first().click();
}

/**
 * Remove a person from a Svelte PersonList. The remove button is only revealed
 * on row hover (waltz-visibility-child), so hover the person's row first.
 */
export async function removePersonViaPersonList(row, personName) {
    const item = row.locator("li", { hasText: personName });
    await item.hover();
    // The remove button carries no accessible name (icon only); match its class.
    await item.locator("button.remove:not([disabled])").click();
}
