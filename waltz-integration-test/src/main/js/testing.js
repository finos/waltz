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

import {login, openApplicationViaSearch, openOrgUnitViaList, openSection} from "./utils.js"
import * as playwright from "playwright";

(async function(){
    const browser = await playwright.chromium.launch({ headless: false }); // Non-headless mode to feel comfy
    const context = await browser.newContext(); // So much to say, but another time
    const page = await context.newPage(); // Create a new Page instance which handles most of your needs
    page.setDefaultTimeout(5000);

    try {
        await page.goto("http://localhost:8000/home");

        await login(page);
        await page.waitForTimeout(200);
        await openOrgUnitViaList(page, "CIO Office");
        await openSection(page, "Attestations");
        // await hoistSection(page, "Attestations");
        await openApplicationViaSearch(page, "Clown Fish - 48");
        await page.waitForTimeout(1000);
    } catch (e) {
        console.log("Error", e.name);
    }

    await browser.close();
})();
