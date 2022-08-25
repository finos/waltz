import {expect, test} from '@playwright/test';
import {clickAndWait, hoistSection, openSection, search, unHoistSection} from "./playwright-test-utils";


test.describe("application section", () => {

    test.beforeEach(async ({page}) => {
        // Go to the starting url before each test.
        await page.goto("/home");
        await search(page, "Test Application");
        const parentUrl = await page.url();
        expect(parentUrl).toContain('application');
    });

    test('Can add an alias to applications', async ({page}) => {
        const addButton = await page.locator(".waltz-alias-list >> a:has-text('add one.'), a:has-text('update') >> visible=true");
        await addButton.click();
        const input = await page.locator(".waltz-alias-list >> tags-input input");
        await input.fill("Test Alias");
        const closeButton = await page.locator(".waltz-alias-list >> button:has-text('Close') >> visible=true");
        await clickAndWait(page, closeButton, "/api/entity/alias/");

        await page.waitForLoadState();
        await page.reload();
        const aliasList = await page.locator(".waltz-alias-list");
        await expect(aliasList).toContainText('Test Alias');
    });

    test('Can add a tag to applications', async ({page}) => {
        const addButton = await page.locator(".waltz-tags-list >> a:has-text('add one.'), a:has-text('update') >> visible=true");
        await addButton.click();
        const input = await page.locator(".waltz-tags-list >> tags-input input");
        await input.fill("Test Tag");
        const closeButton = await page.locator(".waltz-tags-list >> button:has-text('Close') >> visible=true");
        await clickAndWait(page, closeButton, "/api/tag/entity/");

        await page.reload();
        const tagsList = await page.locator(".waltz-tags-list");
        await expect(tagsList).toContainText('Test Tag');
    });

});

