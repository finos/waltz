import { test, expect } from "@playwright/test";
import { apiContext, authenticate, createApp, login, uniqueName } from "./helpers/api.js";
import * as survey from "./helpers/survey.js";

const baseURL = process.env.WALTZ_BASE_URL ?? "http://localhost:8080";

/**
 * Managing survey templates + issuing/administering surveys requires the
 * SURVEY_TEMPLATE_ADMIN and SURVEY_ADMIN roles (the ADMIN role does not imply
 * them in the UI's waltz-has-role checks). Grant them once, preserving any
 * existing roles.
 */
test.beforeAll(async () => {
    const token = await login(baseURL);
    const ctx = await apiContext(baseURL, token);
    await survey.grantSurveyRoles(ctx);
    await ctx.dispose();
});

/**
 * Create a survey template via the UI (Create New form). Leaves the browser on
 * the template edit page and returns the chosen name/externalId.
 */
async function createTemplateViaUi(page, name) {
    const externalId = uniqueName("TEST_SURVEY");

    await page.goto("/survey/template/list");
    await page.locator(".btn", { hasText: "Create New" }).click();

    // Required fields missing -> submit hidden, warning shown.
    const createButton = page.locator("button", { hasText: "Create" });
    await expect(createButton).toBeHidden();
    await expect(page.locator(".alert-warning")).toBeVisible();

    await page.fill("#name", name);
    await page.fill("#externalId", externalId);
    await page.selectOption("#targetEntityKind", { label: "Application" });

    // Once required fields are populated, submit appears and the warning goes.
    await expect(createButton).toBeVisible();
    await expect(page.locator(".alert-warning")).toBeHidden();

    await createButton.click();

    // On success the app navigates to the edit page, which hosts the
    // Questions section.
    const questionsSection = page.locator("waltz-section[name=Questions]");
    await expect(questionsSection).toBeVisible();

    return { name, externalId };
}

/**
 * Add a question to a template on the edit page. Assumes the Questions section
 * is present.
 */
async function addQuestionViaUi(page, { questionText, externalId, helpText }) {
    const questionsSection = page.locator("waltz-section[name=Questions]");
    await questionsSection.locator(".btn", { hasText: "Add New" }).click();

    const questionForm = questionsSection.locator("form[name=surveyQuestionForm]");
    const createQuestionButton = questionForm.locator("button", { hasText: "Create" });

    // Question text is required -> submit hidden, warning shown initially.
    await expect(questionForm.locator(".alert-warning")).toBeVisible();
    await expect(createQuestionButton).toBeHidden();

    await questionForm.locator("#qText").fill(questionText);
    await questionForm.locator("#qExternalId").fill(externalId);
    await questionForm.locator("#qHelpText").fill(helpText);
    await questionForm.locator("#qFieldType").selectOption({ label: "Boolean" });
    await questionForm.locator("#qIsMandatory").setChecked(true);
    // #qPosition is required but pre-filled (defaults to 10) by the controller.

    await expect(createQuestionButton).toBeVisible();
    await expect(questionForm.locator(".alert-warning")).toBeHidden();

    await createQuestionButton.click();
}

// ---------------------------------------------------------------------------
// Template lifecycle (driven entirely through the UI)
// ---------------------------------------------------------------------------

test("creates a survey template targeting Application", async ({ page, context }) => {
    const token = await login(baseURL);
    await authenticate(context, token);

    const name = uniqueName("ts_survey_tmpl");
    await createTemplateViaUi(page, name);

    // Landed on the edit page for the new template.
    await expect(page.getByTestId("header-small")).toHaveText("Edit");
    await expect(page.locator("#name")).toHaveValue(name);
});

test("adds a question to a survey template", async ({ page, context }) => {
    const token = await login(baseURL);
    await authenticate(context, token);

    const name = uniqueName("ts_survey_q");
    await createTemplateViaUi(page, name);

    const questionText = "Simple Question: In Scope?";
    await addQuestionViaUi(page, {
        questionText,
        externalId: uniqueName("SIMPLE_Q"),
        helpText: "Simple Question Help Text"
    });

    // Back in the list view, the new question is shown in the overview table.
    const questionsSection = page.locator("waltz-section[name=Questions]");
    await expect(questionsSection.locator("td").getByText(questionText)).toBeVisible();
});

test("activates a template and exposes lifecycle actions", async ({ page, context }) => {
    const token = await login(baseURL);
    await authenticate(context, token);

    const name = uniqueName("ts_survey_life");
    await createTemplateViaUi(page, name);
    await addQuestionViaUi(page, {
        questionText: "Is this in scope?",
        externalId: uniqueName("IN_SCOPE"),
        helpText: "Help"
    });

    // Navigate from the edit page to the template view via the breadcrumb.
    await page.locator(".waltz-breadcrumbs").getByText(name).click();

    const actions = page.locator(".waltz-page-summary .waltz-section-actions");

    // While DRAFT: "Mark as Active" is offered, the active-only actions are not.
    const markActive = actions.getByText("Mark as Active");
    await expect(markActive).toBeVisible();
    await markActive.click();

    // Once ACTIVE the draft/obsolete/clone actions are available.
    await expect(actions.getByText("Mark as Obsolete")).toBeVisible();
    await expect(actions.getByText("Mark as Draft")).toBeVisible();
    await expect(actions.getByText("Clone")).toBeVisible();
    await expect(markActive).toBeHidden();
});

// ---------------------------------------------------------------------------
// Issuance
// ---------------------------------------------------------------------------

/**
 * Issue a survey run through the multi-step create wizard (the bulk,
 * selector-driven flow). The recipient is derived from an involvement kind, so
 * the admin person is linked to an application inside the selector app group.
 */
test("issues a survey run via the create wizard", async ({ page, context }) => {
    const token = await login(baseURL);
    await authenticate(context, token);
    const ctx = await apiContext(baseURL, token);

    const admin = await survey.getAdminPerson(ctx);
    const involvementKind = await survey.applicationInvolvementKind(ctx);

    // Selector app group holding an application that admin is involved with.
    const app = await createApp(baseURL, token, uniqueName("ts_wiz_app"));
    await survey.addAppInvolvement(ctx, app.id, admin.id, involvementKind.id);
    const group = await survey.createAppGroup(ctx, uniqueName("ts_wiz_grp"));
    await survey.addAppToGroup(ctx, group.id, app.id);

    // Active template to issue.
    const templateId = await survey.createTemplate(ctx, {
        name: uniqueName("ts_wiz_tmpl"),
        externalId: uniqueName("TS_WIZ")
    });
    await survey.addQuestion(ctx, { templateId, questionText: "In scope?", externalId: uniqueName("TS_WIZ_Q") });
    await survey.setTemplateStatus(ctx, templateId, "ACTIVE");

    // --- GENERAL step
    await page.goto(`/survey/run/template/${templateId}/run-create`);
    await page.fill("#name", uniqueName("ts_wiz_run"));
    await page.fill("#email", "e2e@waltz.test");
    await page.fill("input#dueDate", "2030-01-01");
    await page.fill("input#approvalDueDate", "2030-02-01");
    await page.selectOption("#selectorEntityKind", { label: "Application Group" });

    // Selector entity (ui-select) — pick the seeded app group.
    await survey.pickInUiSelect(page.locator("#selectorEntity"), page, group.name, group.name);

    await page.selectOption("#selectorScope", { label: "Exact" });
    await page.selectOption("#involvementKinds", { label: involvementKind.name });
    await page.locator('input[name="issuanceKind"][value="INDIVIDUAL"]').check();

    // The general step stays in the DOM (ng-show) once we advance, so scope the
    // step buttons to their own components to keep locators unambiguous.
    await page.locator("waltz-survey-run-create-general").getByRole("button", { name: "Next" }).click();

    // --- RECIPIENT step: the preview lists admin as a derived recipient.
    const recipientStep = page.locator("waltz-survey-run-create-recipient");
    await expect(recipientStep.locator("td", { hasText: admin.displayName })).toBeVisible();

    await recipientStep.getByRole("button", { name: "Next" }).click();

    // --- COMPLETED step: issuance confirmation shown.
    await expect(page.getByTestId("issuance-confirmation")).toBeVisible();

    await ctx.dispose();
});

/**
 * Issue an individual survey directly from an application's Surveys section
 * (the per-entity flow where recipients are picked by hand).
 */
test("issues an individual survey from an application page", async ({ page, context }) => {
    const token = await login(baseURL);
    await authenticate(context, token);
    const ctx = await apiContext(baseURL, token);

    const admin = await survey.getAdminPerson(ctx);
    const app = await createApp(baseURL, token, uniqueName("ts_ind_app"));

    // Active template to pick in the section form.
    const templateName = uniqueName("ts_ind_tmpl");
    const templateId = await survey.createTemplate(ctx, { name: templateName, externalId: uniqueName("TS_IND") });
    await survey.addQuestion(ctx, { templateId, questionText: "In scope?", externalId: uniqueName("TS_IND_Q") });
    await survey.setTemplateStatus(ctx, templateId, "ACTIVE");

    // Open the Surveys section on the application view. Section nav buttons carry
    // a leading space in their accessible name, so match non-exactly.
    await page.goto(`/application/${app.id}`);
    await page.getByRole("button", { name: "Surveys" }).click();

    const section = page.locator("waltz-survey-section");
    await section.getByText("Issue new survey").click();

    // Choose the seeded template (filter narrows the list when many exist).
    const filter = section.locator('input[type="search"]');
    if (await filter.isVisible()) {
        await filter.fill(templateName);
    }
    // The template rows are hrefless anchors (no link role), so match by text.
    await section.locator("td a", { hasText: templateName }).click();

    // Fill the run form: name defaults from the template; email + due date required.
    const runForm = section.locator("form[name=runForm]");
    await runForm.locator("input[type=text]").first().fill(uniqueName("ts_ind_run"));
    await runForm.locator("input[type=text]").nth(1).fill("e2e@waltz.test");
    await runForm.locator("input#dueDate").fill("2030-01-01");

    // Add admin as an individual recipient via the user pick list.
    const recipientPicker = section.locator("waltz-user-pick-list").first();
    await recipientPicker.getByRole("button", { name: "Add" }).click();
    await survey.pickInUiSelect(recipientPicker, page, admin.displayName, admin.displayName);
    await recipientPicker.getByRole("button", { name: "Save" }).click();

    await runForm.locator('input[name="issuanceKind"][value="INDIVIDUAL"]').check();
    await runForm.getByRole("button", { name: "Issue survey" }).click();

    // Back in the section list, the issued survey appears.
    await expect(section.getByText(admin.displayName).first()).toBeVisible();

    await ctx.dispose();
});

// ---------------------------------------------------------------------------
// Instance lifecycle (submit / approve / reject / reopen / withdraw)
// ---------------------------------------------------------------------------

test("submits and approves a survey response", async ({ page, context }) => {
    const token = await login(baseURL);
    await authenticate(context, token);
    const ctx = await apiContext(baseURL, token);

    const seed = await survey.seedIssuedSurveyForAdmin(baseURL, token, { createApp, uniqueName });

    await page.goto(`/survey/instance/${seed.instanceId}/response/view`);

    await survey.invokeSurveyAction(page, "Submit");
    await expect.poll(() => survey.instanceStatus(ctx, seed.instanceId)).toBe("COMPLETED");

    await survey.invokeSurveyAction(page, "Approve");
    await expect.poll(() => survey.instanceStatus(ctx, seed.instanceId)).toBe("APPROVED");

    await ctx.dispose();
});

test("rejects, reopens and withdraws a survey response", async ({ page, context }) => {
    const token = await login(baseURL);
    await authenticate(context, token);
    const ctx = await apiContext(baseURL, token);

    const seed = await survey.seedIssuedSurveyForAdmin(baseURL, token, { createApp, uniqueName });
    // Advance to COMPLETED via REST so the view opens with approve/reject offered.
    await survey.changeStatus(ctx, seed.instanceId, "SUBMITTING");

    await page.goto(`/survey/instance/${seed.instanceId}/response/view`);

    await survey.invokeSurveyAction(page, "Reject");
    await expect.poll(() => survey.instanceStatus(ctx, seed.instanceId)).toBe("REJECTED");

    // Reopen needs no reason (NOT_REQUIRED) and fires immediately.
    await survey.invokeSurveyAction(page, "Reopen", { needsReason: false });
    await expect.poll(() => survey.instanceStatus(ctx, seed.instanceId)).toBe("IN_PROGRESS");

    await survey.invokeSurveyAction(page, "Withdraw");
    await expect.poll(() => survey.instanceStatus(ctx, seed.instanceId)).toBe("WITHDRAWN");

    await ctx.dispose();
});

// ---------------------------------------------------------------------------
// Reassignment of recipients + owners
// ---------------------------------------------------------------------------

test("reassigns recipients and owners on an instance", async ({ page, context }) => {
    const token = await login(baseURL);
    await authenticate(context, token);
    const ctx = await apiContext(baseURL, token);

    const seed = await survey.seedIssuedSurveyForAdmin(baseURL, token, { createApp, uniqueName });
    const other = await survey.findOtherPerson(ctx, seed.adminPersonId);

    await page.goto(`/survey/instance/${seed.instanceId}/response/view`);

    const peopleTable = page.locator("table:has(td:has-text('Recipients')):has(td:has-text('Individual Approvers'))");
    const recipientRow = peopleTable.locator("tr", { hasText: "Recipients" });
    const ownerRow = peopleTable.locator("tr", { hasText: "Individual Approvers" });

    // Add the other person as a recipient, then remove them.
    await survey.addPersonViaPersonList(recipientRow, page, other.displayName, other.displayName);
    await expect(recipientRow.getByText(other.displayName)).toBeVisible();
    await survey.removePersonViaPersonList(recipientRow, other.displayName);
    await expect(recipientRow.getByText(other.displayName)).toBeHidden();

    // Add the other person as an individual approver (owner), then remove them.
    await survey.addPersonViaPersonList(ownerRow, page, other.displayName, other.displayName);
    await expect(ownerRow.getByText(other.displayName)).toBeVisible();
    await survey.removePersonViaPersonList(ownerRow, other.displayName);
    await expect(ownerRow.getByText(other.displayName)).toBeHidden();

    await ctx.dispose();
});

// ---------------------------------------------------------------------------
// Inclusion predicates
// ---------------------------------------------------------------------------

/**
 * A question with an inclusion predicate is hidden until its predicate is
 * satisfied. Q2 is gated on Q1 being checked; answering Q1 reveals Q2 live
 * (the edit form re-fetches the visible questions after each save).
 */
test("inclusion predicate reveals a dependent question", async ({ page, context }) => {
    const token = await login(baseURL);
    await authenticate(context, token);
    const ctx = await apiContext(baseURL, token);

    const gateExternalId = uniqueName("TS_GATE");
    const seed = await survey.seedIssuedSurveyForAdmin(baseURL, token, {
        createApp,
        uniqueName,
        questions: [
            { questionText: "Gate question?", fieldType: "BOOLEAN", externalId: gateExternalId },
            { questionText: "Follow-up detail", fieldType: "TEXT", inclusionPredicate: `isChecked('${gateExternalId}')` }
        ]
    });

    await page.goto(`/survey/instance/${seed.instanceId}/response/edit`);

    const form = page.locator("form[name=surveyResponseForm]");
    const gate = form.locator(".waltz-survey-question-edit-row", { hasText: "Gate question?" });
    await expect(gate).toBeVisible();
    // The gated question is not shown until the predicate is satisfied.
    await expect(form.getByText("Follow-up detail")).toBeHidden();

    // Answer the gate "Yes" -> the dependent question appears.
    await gate.locator('input[type=radio][value="true"]').check();
    await expect(form.getByText("Follow-up detail")).toBeVisible();

    await ctx.dispose();
});

// ---------------------------------------------------------------------------
// Export
// ---------------------------------------------------------------------------

test("exports a run and an individual response", async ({ page, context }) => {
    const token = await login(baseURL);
    await authenticate(context, token);
    const ctx = await apiContext(baseURL, token);

    const seed = await survey.seedIssuedSurveyForAdmin(baseURL, token, { createApp, uniqueName });

    // --- Run-level export (from the run view "Instances" section actions). The
    // dropdown menu is appended to <body>, so the CSV item sits outside the
    // toggle's button-group — open the toggle, then click the visible item.
    await page.goto(`/survey/run/${seed.runId}`);
    await page.locator(".btn-group", { hasText: "Export Progress" }).getByText("Export Progress").click();
    const [runDownload] = await Promise.all([
        page.waitForEvent("download"),
        page.locator(".dropdown-menu:visible").getByText("Export as csv").click()
    ]);
    expect(runDownload.suggestedFilename()).toContain(".csv");

    // --- Individual response export (from the response view context panel). This
    // is a Svelte hover dropdown whose CSV item fires on mousedown.
    await page.goto(`/survey/instance/${seed.instanceId}/response/view`);
    const exporter = page.locator("li", { hasText: "Export Survey" }).first();
    await exporter.hover();
    const [individualDownload] = await Promise.all([
        page.waitForEvent("download"),
        exporter.getByText("Export as csv").click()
    ]);
    expect(individualDownload.suggestedFilename()).toContain(".csv");

    await ctx.dispose();
});
