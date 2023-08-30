package org.finos.waltz.test_common.playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.finos.waltz.common.IOUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.finos.waltz.common.StringUtilities.mkPath;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DocumentationHelper {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentationHelper.class);

    private final AtomicInteger counter = new AtomicInteger(0);
    private final Page page;
    private final String basePath;
    private final Map<String, String> filenameMap = new HashMap<>();

    private boolean paused = false;


    public DocumentationHelper(Page page, String basePath) {
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


    public DocumentationHelper pause() {
        LOG.debug("Pausing snapshotting");
        this.paused = true;
        return this;
    }


    public DocumentationHelper resume() {
        LOG.debug("Resuming snapshotting");
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
        Set<String> requiredScreenshots = new HashSet<>();

        for (Map.Entry<String, String> entry : filenameMap.entrySet()) {

            Pattern pattern = Pattern.compile(format(
                    "\\{\\{%s\\}\\}",
                    entry.getKey()));

            if (pattern.matcher(templateStr).find()) {
                requiredScreenshots.add(entry.getValue());
                templateStr = pattern
                        .matcher(templateStr)
                        .replaceAll(format(
                                "![%s](%s \"%s\")",
                                entry.getKey(),
                                entry.getValue(),
                                entry.getKey()));
            }
        }

        copyPreparedDocs(
                templateStr,
                requiredScreenshots);

    }


    private void copyPreparedDocs(String templateStr,
                                  Set<String> requiredScreenshots) throws IOException {
        Path screenshotFolder = Paths.get(mkPath("screenshots", basePath));
        Path destFolder = Paths.get(mkPath("docs", basePath));
        Files.createDirectories(destFolder);

        for (String requiredScreenshot : requiredScreenshots) {
            Path src = screenshotFolder.resolve(requiredScreenshot);
            Path dest = destFolder.resolve(requiredScreenshot);
            Files.copy(src, dest, REPLACE_EXISTING);
        }

        Path readmePath = destFolder.resolve("README.md");
        Files.write(readmePath, templateStr.getBytes());

        LOG.info("Documentation produced into: {}", destFolder.toAbsolutePath());
    }
}
