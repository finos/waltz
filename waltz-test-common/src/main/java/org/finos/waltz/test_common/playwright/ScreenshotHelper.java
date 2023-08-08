package org.finos.waltz.test_common.playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.finos.waltz.common.StringUtilities;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static org.finos.waltz.common.StringUtilities.mkPath;

public class ScreenshotHelper {

    private AtomicInteger counter = new AtomicInteger(0);
    private boolean paused = false;

    private final Page page;
    private final String basePath;

    public ScreenshotHelper(Page page, String basePath) {
        this.page = page;
        this.basePath = basePath;
    }


    public Locator takeElemSnapshot(Locator locator, String name) {
        if (! paused) {
            Locator.ScreenshotOptions options = new Locator.ScreenshotOptions()
                    .setPath(mkPath(name));

            locator.screenshot(options);
        }
        return locator;
    }


    public Page takePageSnapshot(Page page, String name) {
        if (! paused) {
            Page.ScreenshotOptions options = new Page.ScreenshotOptions()
                    .setPath(mkPath(name));

            page.screenshot(options);
        }
        return page;
    }


    public Locator takePageSnapshot(Locator locator, String name) {
        locator.waitFor();

        locator.scrollIntoViewIfNeeded();

        if (! paused) {
            Page.ScreenshotOptions options = new Page
                    .ScreenshotOptions()
                    .setPath(mkPath(name));

            page.screenshot(options);
        }

        return locator;
    }


    public ScreenshotHelper pause() {
        this.paused = true;
        return this;
    }


    public ScreenshotHelper resume() {
        this.paused = false;
        return this;
    }


    // -- HELPER ---

    private Path mkPath(String name) {
        return Paths.get(StringUtilities.mkPath(
                basePath,
                format("%d_%s", counter.incrementAndGet(), name)));
    }


}
