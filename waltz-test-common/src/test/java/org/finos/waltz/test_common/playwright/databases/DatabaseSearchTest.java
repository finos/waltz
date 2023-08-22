package org.finos.waltz.test_common.playwright.databases;

import com.microsoft.playwright.Locator;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.test_common.helpers.DatabaseHelper;
import org.finos.waltz.test_common.playwright.BasePlaywrightIntegrationTest;
import org.finos.waltz.test_common.playwright.DocumentationHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.lang.String.format;
import static org.finos.waltz.common.StringUtilities.mkPath;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.finos.waltz.test_common.playwright.PlaywrightUtilities.login;
import static org.finos.waltz.test_common.playwright.PlaywrightUtilities.startSiteSearch;

public class DatabaseSearchTest extends BasePlaywrightIntegrationTest {

    @Autowired
    private DatabaseHelper databaseHelper;

    private EntityReference dbRef = null;


    @BeforeEach
    public void setupAssessmentData() throws IOException {
        login(page, BASE);

        dbRef = databaseHelper.createNewDatabase(
                mkName("databases", "searchDatabase"),
                mkName("databases", "searchInstance"));

        page.navigate(mkPath(BASE, "/home"));
    }


    @Test
    public void exactSearch() {
        DocumentationHelper documentationHelper = new DocumentationHelper(
                page,
                "databases/search");

        startSiteSearch(
                page,
                dbRef.name().orElse("??"));

        Locator resultLocator = page
                .locator(".wnso-search-results")
                .locator(format(
                        "text=%s",
                        dbRef.name().orElse("?")));

        resultLocator.waitFor();

        documentationHelper.takePageSnapshot(
                resultLocator,
                "after-typing.png");

        resultLocator
                .click();

        // wait for search panel to be removed
        assertThat(page.locator(".wnso-search-results")).isHidden();

        Locator dbPageTitleLocator = page
                .locator(".waltz-display-section")
                .locator(format("text=%s", dbRef.name().orElse("?")))
                .first();

        assertThat(dbPageTitleLocator).isVisible();

        documentationHelper.takePageSnapshot(dbPageTitleLocator, "clicked-link.png");
    }
}
