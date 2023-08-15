package org.finos.waltz.test_common.playwright.user_roles;

import com.microsoft.playwright.Locator;
import org.finos.waltz.test_common.helpers.PersonHelper;
import org.finos.waltz.test_common.helpers.UserHelper;
import org.finos.waltz.test_common.playwright.BasePlaywrightIntegrationTest;
import org.finos.waltz.test_common.playwright.ScreenshotHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.finos.waltz.common.StringUtilities.mkPath;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.finos.waltz.test_common.playwright.PlaywrightUtilities.login;

public class UserRoleTests extends BasePlaywrightIntegrationTest {

    public static final String HIGHLIGHT_ELEM_SCRIPT = "d => d.style.border = '2px solid red'";

    @Autowired
    private PersonHelper personHelper;

    @Autowired
    private UserHelper userHelper;

    @Test
    public void registerUserViaUI() throws IOException {
        ScreenshotHelper screenshotHelper = new ScreenshotHelper(
                page,
                "user/registration");

        login(page, BASE);

        page.navigate(mkPath(BASE, "system", "list"));

        Locator userMgmtItem = page.getByTestId("Manage Users");
        assertThat(userMgmtItem).isVisible();
        userMgmtItem.evaluate(HIGHLIGHT_ELEM_SCRIPT);
        screenshotHelper.takePageSnapshot(page, "admin_page.png");

        userMgmtItem.locator("a").click();

        Locator addUserBtn = page.getByTestId("add-user-btn");
        addUserBtn.evaluate(HIGHLIGHT_ELEM_SCRIPT);
        screenshotHelper.takePageSnapshot(page, "add_user_button.png");
        addUserBtn.click();

        screenshotHelper.takePageSnapshot(page, "blank_add_user_form.png");
        Locator submitBtn = page.getByTestId("submit-new-user-btn");
        assertThat(submitBtn).isDisabled();

        String username = mkName("add_user");
        String password = mkName("add_user_pwd");

        page.fill("#username", username);
        page.fill("#password", password);

        assertThat(submitBtn).isEnabled();
        screenshotHelper.takePageSnapshot(page, "completed_add_user_form.png");
        submitBtn.click();

        Locator header = page.locator("h4").getByText(username);
        assertThat(header).isVisible();
        screenshotHelper.takePageSnapshot(page, "user_registered.png");

        screenshotHelper.prepareDocumentation();
    }

}
