package org.finos.waltz.test_common.playwright.flow_classification_rule;

import com.microsoft.playwright.Locator;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.helpers.DataTypeHelper;
import org.finos.waltz.test_common.playwright.BasePlaywrightIntegrationTest;
import org.finos.waltz.test_common.playwright.ScreenshotHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.lang.String.format;
import static org.finos.waltz.common.StringUtilities.mkPath;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.finos.waltz.test_common.helpers.NameHelper.toName;
import static org.finos.waltz.test_common.playwright.PlaywrightUtilities.login;

public class RuleCreationTest extends BasePlaywrightIntegrationTest {

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private DataTypeHelper dataTypeHelper;

    private EntityReference appRef = null;

    @BeforeEach
    public void setupAssessmentData() throws IOException {
        login(page, BASE);
        appRef = appHelper.createNewApp(
                mkName("FlowClassificationRule", "create"),
                10L);

        page.navigate(mkPath(BASE, "data-types"));
    }


    @Test
    public void add() throws InterruptedException {

        ScreenshotHelper screenshotHelper = new ScreenshotHelper(
                page,
                "flow-classification-rule/create");

        Locator listLocator = page.getByTestId("flow-classification-rule-list");

        listLocator.scrollIntoViewIfNeeded();

        screenshotHelper.takePageSnapshot(
                listLocator,
                "show_list.png");

        listLocator
                .getByTestId("create-rule")
                .click();

        Locator formLocator = screenshotHelper.takePageSnapshot(
                page.getByTestId("flow-classification-rule-editor"),
                "create_rule.png");

        formLocator
                .locator("#source")
                .locator(".autocomplete-input")
                .fill(toName(appRef));

        Locator appLocator = screenshotHelper.takePageSnapshot(
                formLocator
                        .locator("#source")
                        .locator(".autocomplete-list-item")
                        .locator(format("text=%s", toName(appRef))),
                "select_app.png");

        appLocator.click();

        screenshotHelper
                .takePageSnapshot(
                    formLocator
                        .locator("#datatype")
                        .locator("text=Book Data"),
                    "select_datatype.png")
                .click();

        formLocator
                .locator("#scope")
                .locator(".autocomplete-input")
                .fill("CEO");

        screenshotHelper
                .takePageSnapshot(
                    formLocator
                            .locator("#scope")
                            .locator(".autocomplete-list-item")
                            .locator("text=CEO Office"),
                    "select_scope.png")
                .click();

        Locator ratingLocator = formLocator
                .locator("#rating")
                .locator("text=Primary Source");

        ratingLocator.click();

        screenshotHelper.takePageSnapshot(
                ratingLocator,
                "select_rating.png");

        formLocator
                .locator("button[type=submit]")
                .click();

        page.locator(".waltz-flow-classification-rules-table input")
                .fill(toName(appRef));

        screenshotHelper.takePageSnapshot(
                page.locator(".waltz-flow-classification-rules-table"),
                "result.png");

        Thread.sleep(1000);

        page.locator(".waltz-flow-classification-rules-table .waltz-entity-icon-label")
                .first()
                .click();

        Thread.sleep(1000);

        screenshotHelper.takePageSnapshot(
                page.getByTestId("source"),
                "view.png");

        assertThat(page.getByTestId("source")).containsText(toName(appRef));
        assertThat(page.getByTestId("data-type")).containsText("Book Data");
        assertThat(page.getByTestId("scope")).containsText("CEO Office");


    }


}
