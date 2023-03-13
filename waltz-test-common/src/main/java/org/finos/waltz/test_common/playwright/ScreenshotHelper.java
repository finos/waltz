package org.finos.waltz.test_common.playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.nio.file.Paths;

import static org.finos.waltz.common.StringUtilities.mkPath;

public class ScreenshotHelper {

    private final Page page;
    private final String basePath;

    public ScreenshotHelper(Page page, String basePath) {
        this.page = page;
        this.basePath = basePath;
    }


    public Locator takeElemSnapshot(Locator locator, String name) {
        locator.screenshot(new Locator
                .ScreenshotOptions()
                .setPath(Paths.get(mkPath(basePath, name))));
        return locator;
    }


    public Locator takePageSnapshot(Locator locator, String name) {

        locator.waitFor();

        page.screenshot(new Page
                .ScreenshotOptions()
                .setPath(Paths.get(mkPath(basePath, name))));
        return locator;
    }
}
