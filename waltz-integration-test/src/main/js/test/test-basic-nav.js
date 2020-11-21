/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

import * as playwright from "playwright";
import {beginWithLogin, login, openApplicationViaSearch, openOrgUnitViaList, openSection} from "./utils.js";

let browser;

before(async() => {
    browser = await playwright.chromium.launch({headless: true});
});

after(async () => {
    await browser.close();
});

let page;

beforeEach(async() => {
    page = await browser.newPage();
    page.setDefaultTimeout(5000);
});

afterEach(async () => {
    await page.close();
});


describe("can perform basic navigation", function () {
    this.timeout(0);

    it('can open org unit page', async () => {
        await beginWithLogin(page);
        await openOrgUnitViaList(page, "CIO Office");
        await openSection(page, "Attestations");
    });

    it('can open app page via search', async () => {
        await beginWithLogin(page);
        await openApplicationViaSearch(page, "Clown Fish - 48");
    });
})
