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

import {login} from "./utils.js"
import * as playwright from "playwright";

(async function(){
    const browser = await playwright.firefox.launch({ headless: false }); // Non-headless mode to feel comfy
    const context = await browser.newContext(); // So much to say, but another time
    const page = await context.newPage(); // Create a new Page instance which handles most of your needs

    console.log("And we're off!")
    await page.goto("http://localhost:8000/home");

    await page.screenshot("foo", "png");

    await login(page);
    await login(page);
    await page.waitForTimeout(3000); // Rest your eyes for five seconds
    await browser.close(); // Close the browser
})();
