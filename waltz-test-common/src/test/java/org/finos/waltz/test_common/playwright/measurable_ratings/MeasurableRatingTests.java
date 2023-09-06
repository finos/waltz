package org.finos.waltz.test_common.playwright.measurable_ratings;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Locator.FilterOptions;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.playwright.BasePlaywrightIntegrationTest;
import org.finos.waltz.test_common.playwright.DocumentationHelper;
import org.finos.waltz.test_common.playwright.Section;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.finos.waltz.common.StringUtilities.mkPath;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.finos.waltz.test_common.playwright.PlaywrightUtilities.login;
import static org.finos.waltz.test_common.playwright.PlaywrightUtilities.mkEmbeddedFrag;

public class MeasurableRatingTests extends BasePlaywrightIntegrationTest {

    public static final String HIGHLIGHT_ELEM_SCRIPT = "d => d.style.border = '2px solid red'";

    @Autowired
    private AppHelper appHelper;

    @Test
    public void editRatingsViaUI() throws IOException {
        DocumentationHelper documentationHelper = new DocumentationHelper(
                page,
                "measurable-ratings/edit-ratings");

        login(page, BASE);

        EntityReference app = appHelper.createNewApp(
                mkName("mr", "edit"),
                10L);

        page.navigate(mkPath(BASE, "application", Long.toString(app.id())));
        Locator sectionButton = page.locator(".sidebar-expanded button").getByText("Ratings / Roadmap");
        sectionButton.evaluate(HIGHLIGHT_ELEM_SCRIPT);
        sectionButton.click();
        Locator section = page.locator(".waltz-measurable-rating-app-section");
        section.isVisible();
        section.scrollIntoViewIfNeeded();
        documentationHelper.takePageSnapshot(page, "section-button.png");

        page.navigate(mkPath(BASE, mkEmbeddedFrag(Section.MEASURABLE_RATINGS, app)));

        Locator ratingsSubSection = page
                .locator(".waltz-sub-section")
                .filter(new FilterOptions().setHasText("Ratings"));

        Locator editBtn = ratingsSubSection.getByText("Edit");
        editBtn.evaluate(HIGHLIGHT_ELEM_SCRIPT);
        documentationHelper.takePageSnapshot(page, "empty-section.png");

        editBtn.click();

        documentationHelper.prepareDocumentation();
    }

}
