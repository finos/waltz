package org.finos.waltz.test_common.playwright.app_group;

import com.microsoft.playwright.Locator;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.test_common.helpers.AppGroupHelper;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.playwright.BasePlaywrightIntegrationTest;
import org.finos.waltz.test_common.playwright.DocumentationHelper;
import org.finos.waltz.test_common.playwright.SearchHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static org.finos.waltz.common.StringUtilities.mkPath;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.finos.waltz.test_common.playwright.PlaywrightUtilities.login;

public class AddAliasesTest extends BasePlaywrightIntegrationTest {

    @Autowired
    private AppGroupHelper appGroupHelper;

    @Autowired
    private AppHelper appHelper;

    @BeforeEach
    public void setup() throws IOException {
        login(page, BASE);
    }

    @Test
    public void addAliases() throws InsufficientPrivelegeException, IOException, InterruptedException {
        String aliasName = mkName("alias");
        String appGroupName = mkName("appGroup_addAlias", "group");
        String appName = mkName("appGroup_addAlias", "application");

        EntityReference app = appHelper.createNewApp(appName, 10L);
        Long groupId = appGroupHelper.createAppGroupWithAppRefs(appGroupName, SetUtilities.asSet(app));
        appGroupHelper.addOwner(groupId, "admin");

        DocumentationHelper documentationHelper = new DocumentationHelper(
                page,
                "app-group/add-aliases");
        page.navigate(mkPath(BASE, "app-group", Long.toString(groupId)));

        Locator summary = page.locator(".waltz-page-summary");
        summary.getByTestId("edit-aliases").evaluate(HIGHLIGHT_ELEM_SCRIPT);
        documentationHelper.takeElemSnapshot(summary, "initial-group.png");

        summary.getByTestId("edit-aliases").first().click();
        Locator aliasesInput = summary.locator(".waltz-alias-list input");
        aliasesInput.fill(aliasName);
        aliasesInput.press("Enter");
        documentationHelper.takeElemSnapshot(summary, "added-alias.png");

        Locator closeBtn = summary.locator(".waltz-alias-list .btn").getByText("Close");
        closeBtn.click();
        documentationHelper.takeElemSnapshot(summary, "view-alias.png");

        SearchHelper searchHelper = new SearchHelper(page);
        searchHelper.search(aliasName);
        documentationHelper.takePageSnapshot(page, "search.png");

        searchHelper.waitForResult(appGroupName);
        documentationHelper.takePageSnapshot(page, "search-result.png");

        documentationHelper.prepareDocumentation();
    }
}
