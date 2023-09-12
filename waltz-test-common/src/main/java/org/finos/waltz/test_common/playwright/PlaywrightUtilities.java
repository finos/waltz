package org.finos.waltz.test_common.playwright;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.RequestOptions;
import org.finos.waltz.model.EntityReference;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;

import static java.lang.String.format;
import static org.finos.waltz.common.MapUtilities.newHashMap;
import static org.finos.waltz.common.StringUtilities.mkPath;

public class PlaywrightUtilities {

    public static void takeScreenshot(Page page,
                                      String path) {
        log("Taking a screenshot: %s", path);
        page.screenshot(new Page
                .ScreenshotOptions()
                .setPath(Paths.get(path)));
    }


    public static void waitAndTakeScreenshot(Page page,
                                             Locator locator,
                                             String imagePath) {
        locator.waitFor();
        takeScreenshot(page, imagePath);
    }


    /**
     * Login using JWT mechanism.
     * <p/>
     * Copies the token provided by the response into the additional headers
     * sent with each subsequent request.
     *
     * @param page
     * @param base
     * @throws IOException
     */
    public static void login(Page page,
                             String base) throws IOException {
        RequestOptions requestOptions = RequestOptions
                .create()
                .setData("{\"userName\":\"admin\", \"password\": \"password\"}");

        APIResponse resp = page
                .context()
                .request()
                .post(
                        mkPath(base, "authentication", "login"),
                        requestOptions);

        HashMap<String, String> loginResult = new ObjectMapper()
                .createParser(resp.text())
                .readValueAs(HashMap.class);

        page.context()
                .setExtraHTTPHeaders(newHashMap(
                        "authorization",
                        format("Bearer %s", loginResult.get("token"))));
    }


    public static String mkEmbeddedFrag(Section section,
                                        EntityReference appRef) {
        return mkPath(
                "embed",
                "internal",
                toFrag(appRef),
                Integer.toString(section.id()));
    }


    public static String toFrag(EntityReference ref) {
        return ref.kind().name() + "/" + ref.id();
    }


    public static void log(String msg, Object... params) {
        System.out.printf(msg + "\n", params);
    }


    public static void logAppLink(EntityReference appRef) {
        log("App link [%s](http://localhost:8000/application/%d)",
                appRef.name().orElse("Un-named App"),
                appRef.id());
    }


}
