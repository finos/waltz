import {expect} from "@playwright/test";

export async function openSection(page, sectionName) {
    const sidebar = await page.locator('.sidebar');
    const sectionButton = sidebar.locator(`.sidenav button:has-text("${sectionName}")`);
    await sectionButton.click();
}


export async function hoistSection(page, sectionId) {
    const section = await page.locator(`.waltz-${sectionId}`);
    const hoistButton = await section.locator("a", {has: page.locator("waltz-icon[title='Embed section']")})
    await hoistButton.click();
    const parentUrl = await page.url();
    await page.waitForLoadState();
    expect(parentUrl).toContain('embed');
}


export async function unHoistSection(context, page) {
    const navigateBackButton = await page.locator("a", {has: page.locator("waltz-icon[name='share-square-o']")});
    navigateBackButton.click() // Opens a new tab
}


export async function search(page, searchText) {
    const navBar = await page.locator(`.navbar`);
    const searchButton = await navBar.locator("a:has-text('Search')");
    await searchButton.click();
    const searchInput = await page.locator(`.wnso-search-region input`);
    searchInput.fill(`"${searchText}"`);
    const searchResult = await page.locator(`.wnso-search-results a:has-text("${searchText}")`);
    await searchResult.click();
}