package org.finos.waltz.test_common.playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.lang.String.format;

public class SearchHelper {

    private final Page page;

    public SearchHelper(Page page) {
        this.page = page;
    }

    public Locator search(String qry) {
        page.locator(".navbar-right")
                .getByTestId("search-button")
                .click();

        Locator searchRegion = page.locator(".wnso-search-region");
        searchRegion.locator("input[type=search]")
                .fill(qry);

        return searchRegion;
    }


    public Locator waitForResult(String name) {
        Locator resultLocator = getSearchResultsPanel()
                .getByTestId("entity-name")
                .getByText(name);

        resultLocator.waitFor();

        return resultLocator;
    }

    public void click(Locator result) {
        result.click();

        // wait for search panel to be removed
        assertThat(page.locator(".wnso-search-results")).isHidden();
    }

    public Locator getSearchResultsPanel() {
        return page.locator(".wnso-search-results");
    }
}
