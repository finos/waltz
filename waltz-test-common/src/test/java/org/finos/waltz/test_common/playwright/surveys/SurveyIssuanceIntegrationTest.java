package org.finos.waltz.test_common.playwright.surveys;


import com.microsoft.playwright.Locator;
import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.survey.ImmutableSurveyQuestion;
import org.finos.waltz.model.survey.SurveyQuestion;
import org.finos.waltz.model.survey.SurveyQuestionFieldType;
import org.finos.waltz.test_common.helpers.AppGroupHelper;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.helpers.InvolvementHelper;
import org.finos.waltz.test_common.helpers.PersonHelper;
import org.finos.waltz.test_common.playwright.BasePlaywrightIntegrationTest;
import org.finos.waltz.test_common.playwright.DocumentationHelper;
import org.finos.waltz.test_common.playwright.Section;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.LocalDate;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.StringUtilities.mkPath;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.finos.waltz.test_common.playwright.PlaywrightUtilities.*;


/**
 * This demonstrates a basic test which uses the integration test helpers
 * to prep test data.
 */
public class SurveyIssuanceIntegrationTest extends BasePlaywrightIntegrationTest {

    @Autowired
    private AppHelper appHelper;


    @Autowired
    private AppGroupHelper appGroupHelper;

    @Autowired
    private InvolvementHelper involvementHelper;

    @Autowired
    private PersonHelper personHelper;


    @BeforeEach
    public void setup() throws IOException {
        login(page, BASE);
    }


    @Test
    public void createTemplate() {
        String name = mkName("surveys-create-template");
        DocumentationHelper documentationHelper = new DocumentationHelper(
                page,
                "survey/create-template");
        createSurveyTemplate(documentationHelper, name);
        activateSurveyTemplate(documentationHelper, name);
    }


    @Test
    public void singleIssuance() {
        String name = mkName("surveys-single-issuance");
        DocumentationHelper documentationHelper = new DocumentationHelper(
                page,
                "survey/single-issuance");

        documentationHelper.pause();

        EntityReference appRef = createAppAndInvolvements(name);

        createSurveyTemplate(documentationHelper, name);
        activateSurveyTemplate(documentationHelper, name);

        documentationHelper.resume();

        // navigate to app surveys and verify no
        page.navigate(mkPath(BASE, mkEmbeddedFrag(Section.APP_SURVEYS, appRef)));
        documentationHelper.takePageSnapshot(page, "surveys_for_app.png");
        assertThat(page.locator("waltz-no-data")).isVisible();

        page.locator(".waltz-section-actions .btn").getByText("Issue new survey").click();
        documentationHelper.takePageSnapshot(page, "issuing_single_survey.png");

        page.locator("input[type=search]").fill(name);
        documentationHelper.takePageSnapshot(page, "searching_for_template.png");

        page.locator("td a").getByText(name).click();
        documentationHelper.takePageSnapshot(page, "template_selected.png");

        page.fill("input#dueDate", LocalDate.now().plusDays(10).toString());
        page.locator("#recipientInvolvementKinds option[label='" + name + "']").click();

        documentationHelper.takePageSnapshot(page, "issuance_params_filled_in.png");
        page.click("form button[type=submit]");

        assertThat(page.locator("table td").getByText(name).first()).isVisible();
        documentationHelper.takePageSnapshot(page, "survey_issued.png");
    }


    @Test
    public void runIssuance() throws InsufficientPrivelegeException {
        String name = mkName("surveys-run-issuance");
        DocumentationHelper documentationHelper = new DocumentationHelper(
                page,
                "survey/run-issuance");

        documentationHelper.pause();

        EntityReference appRef = createAppAndInvolvements(name);
        appGroupHelper.createAppGroupWithAppRefs(name, asSet(appRef));

        createSurveyTemplate(documentationHelper, name);
        activateSurveyTemplate(documentationHelper, name);

        documentationHelper.resume();

        page.navigate(mkPath(BASE, "/survey/template/list"));
        documentationHelper.takePageSnapshot(page, "survey_list.png");
        page.locator("td a").getByText(name).click();
        documentationHelper.takePageSnapshot(page, "template-page.png");
        page.locator("waltz-section[name=Runs] .waltz-section-actions").getByText("Create").click();
        documentationHelper.takePageSnapshot(page, "issuance-params-empty.png");

        Locator nextBtn = page.locator("button").getByText("Next");
        assertThat(nextBtn).isHidden();

        page.fill("#email", "test@waltz.com");
        page.fill("input#dueDate", LocalDate.now().plusDays(10).toString());
        page.fill("input#approvalDueDate", LocalDate.now().plusDays(20).toString());
        page.locator("#involvementKinds option[label=" + name + "]").click();
        searchViaUISelect(name);

        documentationHelper.takePageSnapshot(page.locator("#selectorScope"), "issuance-params-completed.png");
        assertThat(nextBtn).isVisible();
        nextBtn.click();

        assertThat(page.locator("h4").getByText("Recipients")).isVisible();
        documentationHelper.takePageSnapshot(page, "verify_issuance_plan.png");

        assertThat(page.locator("td").getByText(name).first()).isVisible();
        Locator issueBtn = page.locator("waltz-survey-run-create-recipient .btn-success").getByText("Next");
        assertThat(issueBtn).isVisible();
        issueBtn.click();

        assertThat(page.locator("h4").getByText("Issue Survey")).isVisible();
        assertThat(page.getByTestId("issuance-confirmation")).isVisible();
        documentationHelper.takePageSnapshot(page, "issued.png");

    }

    // --- HELPERS ---

    private EntityReference createAppAndInvolvements(String name) {
        EntityReference appRef = appHelper.createNewApp(
                name,
                10L);
        long invKindId = involvementHelper.mkInvolvementKind(name);
        long personId = personHelper.createPerson(name);
        involvementHelper.createInvolvement(personId, invKindId, appRef);
        return appRef;
    }


    private void searchViaUISelect(String name) {
        page.locator(".ui-select-match").click();
        page.locator("input.ui-select-search").fill(name);
        page.locator(".ui-select-highlight").getByText(name).click();
    }


    private void activateSurveyTemplate(DocumentationHelper documentationHelper, String name) {
        // navigate back to overview so we can activate the survey
        page.locator(".waltz-breadcrumbs").getByText(name).click();
        documentationHelper.takePageSnapshot(page, "overview_in_prep_for_marking_as_active.png");
        Locator actionBtns = page.locator(".waltz-page-summary .waltz-section-actions .btn");

        actionBtns.getByText("Mark as Active").click();

        assertThat(actionBtns.getByText("Mark as Obsolete")).isVisible();
        assertThat(actionBtns.getByText("Mark as Draft")).isVisible();
        assertThat(actionBtns.getByText("Clone")).isVisible();

        documentationHelper.takePageSnapshot(page, "template_is_active.png");
    }


    private void createSurveyTemplate(DocumentationHelper documentationHelper,
                                      String name) {
        log("Creating survey template: %s", name);
        page.navigate(mkPath(BASE, "/survey/template/list"));
        page.locator(".btn").getByText("Create New").click();

        // check the warning about incompleteness is visible and the submit button is hidden
        Locator createTemplateButton = page.locator("button").getByText("Create");
        assertThat(createTemplateButton).isHidden();
        assertThat(page.locator(".alert-warning")).isVisible();

        // fill in the form
        documentationHelper.takePageSnapshot(page, "create_new.png");
        page.fill("#name", name);
        page.fill("#externalId", mkName("TEST_SURVEY"));
        page.selectOption("#targetEntityKind", "Application");
        documentationHelper.takePageSnapshot(page, "create_form_filled_in.png");

        // check the submit button is visible and the warning has been hidden
        assertThat(createTemplateButton).isVisible();
        assertThat(page.locator(".alert-warning")).isHidden();

        // create the survey and wait for the questions section
        createTemplateButton.click();
        Locator questionsSection = page.locator("waltz-section[name=Questions]");
        questionsSection.waitFor();
        documentationHelper.takeElemSnapshot(questionsSection, "ready_to_add_questions.png");

        addQuestion(
                documentationHelper,
                questionsSection,
                ImmutableSurveyQuestion
                    .builder()
                    .surveyTemplateId(-1L) // doesn't matter
                    .questionText("Simple Question: In Scope?")
                    .externalId("SIMPLE_QUESTION_IN_SCOPE")
                    .fieldType(SurveyQuestionFieldType.BOOLEAN)
                    .isMandatory(true)
                    .helpText("Simple Question Help Text")
                    .build(),
                "q1");

        addQuestion(
                documentationHelper,
                questionsSection,
                ImmutableSurveyQuestion
                    .builder()
                    .surveyTemplateId(-1L) // doesn't matter
                    .questionText("Please give more detail")
                    .externalId("PREDICATE_QUESTION_DETAIL")
                    .fieldType(SurveyQuestionFieldType.TEXTAREA)
                    .isMandatory(true)
                    .inclusionPredicate("isChecked('SIMPLE_QUESTION_IN_SCOPE', false)")
                    .helpText("Predicate Detail Question Help Text")
                    .build(),
                "q2");

    }


    private void addQuestion(DocumentationHelper documentationHelper,
                             Locator questionsSection,
                             SurveyQuestion question,
                             String snapshotStem) {
        // add a simple question
        questionsSection.locator(".waltz-section-actions .btn-primary").getByText("Add New").click();
        documentationHelper.takeElemSnapshot(questionsSection, snapshotStem + "_adding_question.png");
        Locator questionForm = questionsSection.locator("form[name=surveyQuestionForm]");

        Locator createQuestionButton = questionsSection.locator("button").getByText("Create");
        assertThat(questionForm.locator(".alert-warning")).isVisible();
        assertThat(createQuestionButton).isHidden();

        fillInQuestionForm(
                questionForm,
                question);

        documentationHelper.takePageSnapshot(questionsSection, snapshotStem + "_question_detail_filled_in.png");
        assertThat(createQuestionButton).isVisible();
        assertThat(questionForm.locator(".alert-warning")).isHidden();
        createQuestionButton.click();
    }


    private void fillInQuestionForm(Locator questionForm,
                                    SurveyQuestion q) {
        questionForm.locator("#qText").fill(q.questionText());
        questionForm.locator("#qExternalId").fill(q.externalId().orElse(""));
        questionForm.locator("#qHelpText").fill(q.helpText().orElse(""));
        questionForm.locator("#qFieldType").selectOption("string:" + q.fieldType().name());
        questionForm.locator("#qIsMandatory").setChecked(q.isMandatory());
    }


}
