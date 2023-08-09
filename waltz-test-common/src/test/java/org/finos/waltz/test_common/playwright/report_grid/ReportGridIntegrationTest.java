package org.finos.waltz.test_common.playwright.report_grid;


import com.microsoft.playwright.Locator;
import org.finos.waltz.common.SetUtilities;
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
import org.finos.waltz.test_common.playwright.ScreenshotHelper;
import org.finos.waltz.test_common.playwright.Section;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.LocalDate;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.lang.String.format;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.StringUtilities.mkPath;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.finos.waltz.test_common.playwright.PlaywrightUtilities.*;


public class ReportGridIntegrationTest extends BasePlaywrightIntegrationTest {

    @Autowired
    private AppHelper appHelper;


    @Autowired
    private AppGroupHelper appGroupHelper;


    @BeforeEach
    public void setup() throws IOException {
        login(page, BASE);
    }


    @Test
    public void createGrid() throws InsufficientPrivelegeException {
        String name = mkName("report-grid-create-grid");
        ScreenshotHelper screenshotHelper = new ScreenshotHelper(
                page,
                "screenshots/report-grid/create-grid");

        EntityReference appRef = appHelper.createNewApp(name, 10L);
        Long gId = appGroupHelper.createAppGroupWithAppRefs(name, asSet(appRef));

        page.navigate(mkPath(BASE, "app-group", Long.toString(gId)));
        screenshotHelper.takePageSnapshot(page, "new_app_group.png");

        page.locator(".sidenav button").getByText("Report Grids").click();
        screenshotHelper.takePageSnapshot(page, "grid_section_opened.png");

        page.locator("a").getByTitle("Embed section").click();
        screenshotHelper.takePageSnapshot(page, "hoisted.png");

        page.locator("button").getByText("Create a new report grid").click();
        screenshotHelper.takePageSnapshot(page, "new_grid_form.png");

        Locator form = page.locator("form");
        form.locator("#title").fill(name);
        form.locator("#description").fill(name);
        screenshotHelper.takeElemSnapshot(form, "completed_form.png");

        form.locator("button[type=submit]").click();
        screenshotHelper.takePageSnapshot(page, "submitted_form.png");

        addColumn(screenshotHelper, "Application", "Asset Kind");
        addColumn(screenshotHelper, "Org Unit", "Name");
        addColumn(screenshotHelper, "Org Unit", "External Id");
        addColumn(screenshotHelper, "Cost Kind", "Application Development");
        addColumn(screenshotHelper, "Attestation ", "Logical Flow");
        addColumn(screenshotHelper, "Involvement Kind ", "IT Architect");

        page.locator(".btn-success").getByText("Save this Report").click();
        screenshotHelper.takePageSnapshot(page, "saved_report.png");

        Locator grid = page.locator("waltz-grid-with-search");
        screenshotHelper.takeElemSnapshot(grid, "grid_data.png");
        assertThat(grid.locator("div.ui-grid-cell").getByTestId("entity-name").getByText(name)).isVisible();
    }


    private void addColumn(ScreenshotHelper screenshotHelper,
                           String category,
                           String option) {
        Locator tab = page.locator(".wt-tab.wt-active");
        tab.locator(".btn").getByText("Select an entity kind").click();
        screenshotHelper.takePageSnapshot(page, format("select_col_category_%s.png", category));
        page.locator(".btn-group .btn").getByText(category, new Locator.GetByTextOptions().setExact(true)).click();
        screenshotHelper.takePageSnapshot(page, format("select_col_%s_%s.png", category, option));
        page.locator("tr.clickable").getByText(option, new Locator.GetByTextOptions().setExact(true)).first().click();
        screenshotHelper.takePageSnapshot(page, format("added_col_%s_%s.png", category, option));
        tab.locator(".btn").getByText("Close").click();
    }

}
