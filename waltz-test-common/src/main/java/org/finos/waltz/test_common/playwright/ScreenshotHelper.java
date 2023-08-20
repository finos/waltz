package org.finos.waltz.test_common.playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.finos.waltz.common.IOUtilities;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static org.finos.waltz.common.StringUtilities.mkPath;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ScreenshotHelper {

    private AtomicInteger counter = new AtomicInteger(0);
    private boolean paused = false;

    private final Page page;
    private final String basePath;
    private final Map<String, String> filenameMap = new HashMap<>();

    public ScreenshotHelper(Page page, String basePath) {
        this.page = page;
        this.basePath = basePath;
    }


    public Locator takeElemSnapshot(Locator locator, String name) {
        if (! paused) {
            Locator.ScreenshotOptions options = new Locator.ScreenshotOptions()
                    .setPath(mkNameWithStep(name));

            locator.screenshot(options);
        }
        return locator;
    }


    public Page takePageSnapshot(Page page, String name) {
        if (! paused) {
            Page.ScreenshotOptions options = new Page.ScreenshotOptions()
                    .setPath(mkNameWithStep(name));

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
                    .setPath(mkNameWithStep(name));

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

    private Path mkNameWithStep(String name) {
        String nameWithStep = format("%d_%s", counter.incrementAndGet(), name);
        filenameMap.put(name, nameWithStep);
        return Paths.get(mkPath(
                "screenshots",
                basePath,
                nameWithStep));
    }


    public void prepareDocumentation() throws IOException {
        String templatePath = mkPath(basePath, "template.md");
        InputStream templateStream = this.getClass().getClassLoader().getResourceAsStream(templatePath);

        assertNotNull(templateStream, String.format("Could not find template on classpath: %s", templatePath));

        String templateStr = IOUtilities.readAsString(templateStream);
        for (Map.Entry<String, String> entry : filenameMap.entrySet()) {
            templateStr = templateStr.replaceAll(
                    format("\\{\\{%s\\}\\}", entry.getKey()),
                    format("![%s](%s \"%s\")", entry.getKey(), entry.getValue(), entry.getKey()));
        }

        System.out.println(templateStr);

        String outputPath = mkPath("screenshots", basePath, "README.md");
        FileWriter fw = new FileWriter(outputPath);
        fw.write(templateStr);
        fw.close();
    }
}
