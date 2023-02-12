package org.finos.waltz.test_common.playwright.assessments;


import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.test_common.playwright.BasePlaywrightTest;
import org.finos.waltz.test_common.playwright.Section;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.finos.waltz.common.StringUtilities.mkPath;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.test_common.playwright.PlaywrightUtilities.*;


/**
 * This demonstrates a basic test without any setup data being created via
 * the integration helpers.  The main advantage of this is during initial
 * test creation as the executuion time is faster.
 */
public class AssessmentCreationAndRemovalTest extends BasePlaywrightTest {

    @Test
    public void addAndRemoveAssessment() throws IOException {
        EntityReference appRef = mkRef(EntityKind.APPLICATION, 100L);

        login(page, BASE);

        log("Opening assessment section in new page");
        page.navigate(mkPath(BASE, mkEmbeddedFrag(Section.ASSESSMENTS, appRef)));

        log("Opening Information Classification Assessment");
        page.waitForSelector(".fa-caret-down");
        page.locator("text=Not Rated").first().click();
        page.locator("text=Information Classification").click();

        log("Filling in a rating");
        page.locator("text=Add").click();
        page.locator("#rating-dropdown").selectOption("Confidential");
        page.locator("#comment").fill("Test comment\n\n- One\n- Two");
        takeScreenshot(page, "screenshots/assessments/create-form.png");

        log("Submitting");
        page.locator("text=Save").click();
        takeScreenshot(page, "screenshots/assessments/post-create.png");

        log("Removing the rating");
        page.locator(".sub-section").locator(".cell").click();
        page.locator("text=Remove").click();
        takeScreenshot(page, "screenshots/assessments/removal-confirmation.png");

        log("Removing (confirmation)");
        page.locator("button").locator("text=Remove").click();
        takeScreenshot(page, "screenshots/assessments/post-remove.png");
    }

}
