package org.finos.waltz.test_common.playwright.applications;

import com.microsoft.playwright.Locator;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.playwright.BasePlaywrightIntegrationTest;
import org.finos.waltz.test_common.playwright.DocumentationHelper;
import org.finos.waltz.test_common.playwright.SearchHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.lang.String.format;
import static org.finos.waltz.common.StringUtilities.mkPath;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.finos.waltz.test_common.playwright.PlaywrightUtilities.login;

public class ApplicationSearchTest extends BasePlaywrightIntegrationTest {

    @Autowired
    private AppHelper appHelper;

    private EntityReference appRef = null;


    @BeforeEach
    public void setupAssessmentData() throws IOException {
        login(page, BASE);
        appRef = appHelper.createNewApp(
                mkName("TestApplication", "searcher"),
                10L);

        page.navigate(mkPath(BASE, "/home"));
    }


    @Test
    public void searchForExactMatch() {
        DocumentationHelper documentationHelper = new DocumentationHelper(
                page,
                "applications/search");

        SearchHelper searchHelper = new SearchHelper(page);
        searchHelper.search(appRef.name().orElse("??"));


        Locator result = searchHelper.waitForResult(appRef.name().orElse("?"));

        documentationHelper.takePageSnapshot(
                result,
                "after-typing.png");

        searchHelper.click(result);

        Locator appPageTitleLocator = page
                .locator(".waltz-page-header")
                .getByTestId("header-small")
                .locator(format("text=%s", appRef.name().orElse("?")));

        assertThat(appPageTitleLocator).isVisible();

        documentationHelper.takePageSnapshot(appPageTitleLocator, "clicked-link.png");
    }

}
