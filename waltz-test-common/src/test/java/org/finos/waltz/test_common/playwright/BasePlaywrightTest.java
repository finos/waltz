package org.finos.waltz.test_common.playwright;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import static org.finos.waltz.common.LoggingUtilities.configureLogging;

public abstract class BasePlaywrightTest {

    protected static final String BASE = "http://localhost:8000";

    protected static Playwright playwright;
    protected static Browser browser;

    // New instance for each test method.
    protected BrowserContext context;
    protected Page page;


    @BeforeAll
    static void launchBrowser() {
        configureLogging();
        playwright = Playwright.create();
        browser = playwright
                .chromium()
                .launch(new BrowserType.LaunchOptions()
                        .setHeadless(true));

    }


    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }


    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();
        context.setDefaultTimeout(2_000);
        page = context.newPage();
    }


    @AfterEach
    void closeContext() {
        context.close();
    }

}
