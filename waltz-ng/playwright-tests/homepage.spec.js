import {test, expect} from '@playwright/test';
import _ from "lodash";

const menuItems = [
    "Actors",
    "Attestations",
    "Licence Management",
    "Overlay Diagrams",
    "Source Data Ratings",
    "Surveys"
];

test.describe("home page", () => {

    test.beforeEach(async ({page}) => {
        // Go to the starting url before each test.
        await page.goto("/home");
        const url = await page.url();
        expect(url).toContain('home');
    });

    test('User visible in the user menu', async ({page}) => {
        const user = await page.locator('[data-test-id=user-id]');
        await expect(user).toHaveText('anonymous');
    });

    test('User menu should display all dropdown menu (excluding admin)', async ({page}) => {
        const userMenu = await page.locator('.waltz-navbar-profile');
        await userMenu.locator(".dropdown-toggle").click();

        const items = await userMenu.locator(".dropdown-menu li a");
        const texts = await items.allTextContents();
        const trimmedText = texts.map(t => t.trim())
        expect(_.difference(menuItems, trimmedText)).toEqual([]);
    });
});
