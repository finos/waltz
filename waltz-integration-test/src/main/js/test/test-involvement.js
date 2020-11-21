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
import {beginWithLogin, login, openApplicationViaSearch, openSection} from "./utils.js";

let browser;

before(async() => {
    browser = await playwright.chromium.launch({headless: false});
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


describe.only("can modify people associated to entities", function () {
    this.timeout(0);
    it("can add person to an app", async () => {
        await beginWithLogin(page);

        await openApplicationViaSearch(page, "Armadillo - 18");
        await openSection(page, "People")

        await page.click("#people-section [data-ux=involvement-edit]");
        await page.click("#people-section .waltz-entity-selector .ui-select-toggle");
        // this is odd!, the ui-select-search moves the input and result elements to the bottom
        // of the dom - outside the original parent elem!
        await page.fill(".waltz-entity-selector .ui-select-search", "Emily");
        await page.click(".waltz-entity-selector .ui-select-choices-row >> 'Emily'");

        // HERE
        await page.waitForTimeout(3000);
    })
});