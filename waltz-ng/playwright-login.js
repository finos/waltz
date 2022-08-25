const {chromium, expect} = require('@playwright/test');
import _ from "lodash";

module.exports = async config => {

    console.log("Logging in");

    const {baseURL, storageState} = config.projects[0].use;
    const browser = await chromium.launch();
    const page = await browser.newPage();

    const [settingsCall] = await Promise
        .all([
            page.waitForResponse(resp => resp.url().includes("api/settings") && resp.status() === 200),
            page.goto(baseURL)]);

    const settings = await settingsCall.json();

    const useWaltzLogin = _.find(settings, d => d.name === "web.authentication") === 'waltz';

    if (!useWaltzLogin) {
        console.log("used custom login")
    } else {
        console.log("use login button")
    }

    await page.context().storageState({path: storageState});
    await browser.close();
};