package org.finos.waltz.test_common.playwright.bookmarks;


import org.finos.waltz.model.EntityReference;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.playwright.BasePlaywrightIntegrationTest;
import org.finos.waltz.test_common.playwright.Section;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static org.finos.waltz.common.StringUtilities.mkPath;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.finos.waltz.test_common.playwright.PlaywrightUtilities.*;


/**
 * This demonstrates a basic test which uses the integration test helpers
 * to prep test data.
 */
public class BookmarkCreationAndRemovalIntegrationTest extends BasePlaywrightIntegrationTest {

    @Autowired
    private AppHelper appHelper;

    private EntityReference appRef;


    @BeforeEach
    public void setupAssessmentData() throws IOException {
        appRef = appHelper.createNewApp(
                mkName("test_app_bookmarks"),
                10L);

        logAppLink(appRef);
        login(page, BASE);

        log("Opening bookmarks section in new page");
        page.navigate(mkPath(BASE, mkEmbeddedFrag(Section.BOOKMARKS, appRef)));
    }


    @Test
    public void addAndRemoveAssessment() {
        createBookmark("Test bookmark");
        removeBookmark();
    }


    // -- HELPERS ------

    private void removeBookmark() {
        log("Making some bookmarks");
        createBookmark("Test bookmark Alpha");
        createBookmark("Test bookmark Bravo");
        createBookmark("Test bookmark Charlie");
        createBookmark("Test bookmark Delta");
        createBookmark("Test bookmark Echo");
        createBookmark("Test bookmark Foxtrot");

        log("Removing bookmark");
        page.locator("input[type=search]").fill("Bookmark Alpha");
        page.locator("text=Remove").click();
        takeScreenshot(page, "screenshots/bookmarks/removal-form.png");

        log("Confirming removal");
        page.locator("button.btn-warning").click();
        page.locator("input[type=search]").clear();
        takeScreenshot(page, "screenshots/bookmarks/post-removal.png");

        page.locator("input[type=search]").fill("Bookmark Alpha");
        page.locator("text=No bookmarks");
    }

    private void createBookmark(String bookmarkName) {
        log("Adding bookmark");
        page.locator("text=Add bookmark").first().click();
        page.locator("input#title").fill(bookmarkName);
        page.locator("input#url").fill("http://finos.org");
        takeScreenshot(page, "screenshots/bookmarks/create-form.png");

        page.locator("button[type=submit]").click();
        takeScreenshot(page, "screenshots/bookmarks/created.png");
    }

}
