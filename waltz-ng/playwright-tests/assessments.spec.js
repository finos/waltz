import {expect, test} from '@playwright/test';
import {
    clickAndWait, clickAndWaitForMethod,
    hoistSection,
    openSection,
    search,
    unHoistSection,
    waitForAnimationEnd
} from "./playwright-test-utils";


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

        await Promise.all([
            page.waitForLoadState('load'),
            page.waitForLoadState('networkidle'),
            page.locator("[data-ux='Toggle Group-caret-down-button'] >> visible=true")]);

        // await page.waitForTimeout(500);

        const icon = await page.locator(".assessment-group-header:has-text('Toggle Group') >> button:visible >> .icon");

        await expect(icon).toHaveAttribute("data-ux", "caret-down");

        await page.click("[data-ux='Toggle Group-caret-down-button'] >> visible=true");

        await expect(icon).toHaveAttribute("data-ux", "caret-right");
        await page.click("[data-ux='Toggle Group-caret-right-button'] >> visible=true");

        await expect(icon).toHaveAttribute("data-ux", "caret-down");
    });


    test('Can favourite assessment and shows in overview', async ({context, page}) => {

        await page.waitForLoadState();

        const assessmentGroup = await page.locator(".assessment-group:has-text('Edit Favourites')");
        const assessment = await assessmentGroup.locator("tr:has-text('Test Definition B')");
        const favouriteButton = await assessment.locator("button:visible");

        await waitForAnimationEnd(page, ".assessment-group:has-text('Edit Favourites')");

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

        const assessmentInOverview = await newPage.locator("waltz-assessment-rating-favourites-list >> tr:has-text('Test Definition B')");
        await expect(assessmentInOverview).toHaveCount(1);
    });


    test('Can unfavourite assessment and removed from overview', async ({context, page}) => {

        await page.waitForLoadState();

        const assessmentGroup = await page.locator(".assessment-group:has-text('Edit Favourites')");
        const assessment = await assessmentGroup.locator("tr:has-text('Test Definition C')");
        const favouriteButton = await assessment.locator("button:visible");

        const favouriteResponse = await clickAndWait(page, favouriteButton, '/api/user-preference/save');
        await favouriteResponse.finished();

        const icon = await assessment.locator("button:visible .icon");
        await expect(icon).toHaveAttribute("data-ux", "star-o");

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


    test('Can open and close subsection', async ({context, page}) => {

        const assessmentGroup = page.locator(".assessment-group:has-text('Update Rating')");
        const assessment = assessmentGroup.locator("tr:has-text('Test Definition E')");

        await page.waitForLoadState();

        await assessment.click();
        const subsectionName = page.locator(".sub-section >> [slot=header]");
        await expect(subsectionName).toContainText("Test Definition E");

        const closeButton = page.locator(".sub-section .controls >> button:has-text('Close')");
        await closeButton.click();
        await expect(subsectionName).toHaveCount(0);

    });


    test('Can update rating on assessment', async ({context, page}) => {

        const assessmentGroup = page.locator(".assessment-group:has-text('Update Rating')");
        const assessment = assessmentGroup.locator("tr:has-text('Test Definition E')");

        await page.waitForLoadState();

        await assessment.click();
        const subsectionName = page.locator(".sub-section >> [slot=header]");
        await expect(subsectionName).toContainText("Test Definition E");
        const assessmentRating = page.locator(".sub-section >> table tr:has-text('Rating')");
        await expect(assessmentRating).toContainText("Yes");
        const editButton = page.locator(".sub-section .controls >> button:has-text('Edit')");
        await editButton.click();
        const ratingDropdown = page.locator(".sub-section >> select#rating-dropdown");
        await ratingDropdown.selectOption({label: 'No'});
        const saveButton = page.locator(".sub-section .controls >> button:has-text('Save')");
        await saveButton.click();
        await expect(assessment).toContainText("No");
    });


    test('Can delete a rating on assessment', async ({context, page}) => {

        const assessmentGroup = page.locator(".assessment-group:has-text('Delete Rating')");
        const assessment = assessmentGroup.locator("tr:has-text('Test Definition F')");

        await page.waitForLoadState();

        await assessment.click();
        const subsectionName = page.locator(".sub-section >> [slot=header]");
        await expect(subsectionName).toContainText("Test Definition F");
        const assessmentRating = page.locator(".sub-section >> table tr:has-text('Rating')");
        await expect(assessmentRating).toContainText("Yes");
        const removeButton = page.locator(".sub-section .controls >> button:has-text('Remove')");
        await removeButton.click();
        const removeConfirmButton = page.locator(".removal-warning >> button:has-text('Remove')");

        await clickAndWaitForMethod(page, removeConfirmButton, "/api/assessment-rating/entity/", "DELETE");

        const notProvidedAssessment = page.locator(".assessment-group:has-text('Delete Rating') >> tr:has-text('Not Rated')");
        await expect(notProvidedAssessment).toContainText("Test Definition F");
    });

});

