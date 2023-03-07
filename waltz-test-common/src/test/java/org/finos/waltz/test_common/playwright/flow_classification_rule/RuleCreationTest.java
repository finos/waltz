package org.finos.waltz.test_common.playwright.flow_classification_rule;

import com.microsoft.playwright.Locator;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.helpers.DataTypeHelper;
import org.finos.waltz.test_common.playwright.BasePlaywrightIntegrationTest;
import org.finos.waltz.test_common.playwright.ScreenshotHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static org.finos.waltz.common.StringUtilities.mkPath;
import static org.finos.waltz.test_common.playwright.PlaywrightUtilities.*;

public class RuleCreationTest extends BasePlaywrightIntegrationTest {

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private DataTypeHelper dataTypeHelper;


    @BeforeEach
    public void setupAssessmentData() throws IOException {
        login(page, BASE);
        page.navigate(mkPath(BASE, "data-types"));
    }


    @Test
    public void add() throws InterruptedException {

        ScreenshotHelper screenshotHelper = new ScreenshotHelper(
                page,
                "screenshots/flow-classification-rule/create");

        Locator listLocator = page.getByTestId("flow-classification-rule-list");

        listLocator.scrollIntoViewIfNeeded();

        screenshotHelper.takePageSnapshot(
                listLocator,
                "1_show_list.png");

        listLocator
                .getByTestId("create-rule")
                .click();

        Locator formLocator = screenshotHelper.takePageSnapshot(
                page.getByTestId("flow-classification-rule-editor"),
                "2_create_rule.png");

        formLocator
                .locator("#source")
                .locator(".autocomplete-input")
                .fill("test");

        Locator appLocator = screenshotHelper.takePageSnapshot(
                formLocator
                        .locator("#source")
                        .locator(".autocomplete-list-item")
                        .locator("text=Application"),
                "3_select_app.png");

        appLocator.click();

        screenshotHelper
                .takePageSnapshot(
                    formLocator
                        .locator("#datatype")
                        .locator("text=Book Data"),
                    "4_select_datatype.png")
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
                    "5_select_scope.png")
                .click();

        Locator ratingLocator = formLocator
                .locator("#rating")
                .locator("text=Primary Source");

        ratingLocator.click();

        screenshotHelper.takePageSnapshot(
                ratingLocator,
                "6_select_rating.png");

        formLocator
                .locator("button[type=submit]")
                .click();

        page.locator(".waltz-flow-classification-rules-table input")
                .fill("Test Application");

        screenshotHelper.takePageSnapshot(
                page.locator(".waltz-flow-classification-rules-table"),
                "7_result.png");

        Thread.sleep(400);
        takeScreenshot(page, "screenshots/flow-classification-rule/a.png");

    }



}
