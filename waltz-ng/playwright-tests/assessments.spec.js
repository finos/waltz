import {expect, test} from '@playwright/test';
import {clickAndWait, hoistSection, openSection, search, unHoistSection} from "./playwright-test-utils";


const hoistAssessmentsSection = async page => {
    await openSection(page, "Assessment Ratings");
    const section = await page.locator(`.waltz-assessment-rating-section`);
    await hoistSection(page, section);
};

test.describe("assessments section", () => {

    test.beforeEach(async ({page}) => {
        // Go to the starting url before each test.
        await page.goto("/home");
        await search(page, "Test Application");
        const parentUrl = await page.url();
        expect(parentUrl).toContain('application');
        await hoistAssessmentsSection(page);
    });

    test('Can toggle assessment group list', async ({page}) => {
        const assessmentGroup = await page.locator(".assessment-group:has-text('Toggle Group')");
        const toggle = await assessmentGroup.locator(".assessment-group-header button:visible");
        const icon = await toggle.locator(".icon");
        await expect(icon).toHaveAttribute("data-ux", "caret-down");
        await toggle.click();
        await expect(icon).toHaveAttribute("data-ux", "caret-right");
        await toggle.click();
        await expect(icon).toHaveAttribute("data-ux", "caret-down");
    });

    test('Can favourite assessment and shows in overview', async ({context, page}) => {

        const assessmentGroup = await page.locator(".assessment-group:has-text('Uncategorized')");
        const assessment = await assessmentGroup.locator("tr:has-text('Test Definition B')");
        const favouriteButton = await assessment.locator("button:visible");

        const favouriteResponse = await clickAndWait(page, favouriteButton, '/api/user-preference/save');
        await favouriteResponse.finished();

        const icon = await assessment.locator("button:visible .icon");
        await expect(icon).toHaveAttribute("data-ux", "star");

        const [newPage] = await Promise.all([
            context.waitForEvent('page'),
            unHoistSection(page)
        ])

        await newPage.waitForLoadState();

        const parentUrl = await newPage.url();
        expect(parentUrl).not.toContain('embed');
        expect(parentUrl).toMatch(/application/i);

        const assessmentInOverview = await newPage.locator("waltz-assessment-rating-favourites-list >> tr:has-text('Test Definition C')");
        await expect(assessmentInOverview).toHaveCount(1);
    });

    test('Can favourite assessment and shows in overview', async ({context, page}) => {

        const assessmentGroup = await page.locator(".assessment-group:has-text('Uncategorized')");
        const assessment = await assessmentGroup.locator("tr:has-text('Test Definition C')");
        const favouriteButton = await assessment.locator("button:visible");

        const favouriteResponse = await clickAndWait(page, favouriteButton, '/api/user-preference/save');
        await favouriteResponse.finished();

        const icon = await assessment.locator("button:visible .icon");
        await expect(icon).toHaveAttribute("data-ux", "star-0");

        const [newPage] = await Promise.all([
            context.waitForEvent('page'),
            unHoistSection(page)
        ])

        await newPage.waitForLoadState();

        const parentUrl = await newPage.url();
        expect(parentUrl).not.toContain('embed');
        expect(parentUrl).toMatch(/application/i);

        const assessmentInOverview = await newPage.locator("waltz-assessment-rating-favourites-list >> tr:has-text('Test Definition C')");
        await expect(assessmentInOverview).toHaveCount(0);
    });


});

