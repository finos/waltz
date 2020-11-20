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

export async function login(page) {
    const menuText = await page.textContent(".waltz-navbar-profile >> a");
    console.log(menuText, menuText.indexOf("Anonymous"));
    if (menuText.indexOf("Anonymous") > -1) {
        console.log("Logging in")
        await page.click(".waltz-navbar-profile");
        await page.click(".waltz-navbar-profile >> 'Login'");
        await page.waitForSelector("form[name=loginForm]");
        await page.fill("form[name=loginForm] >> input[ng-model=username]", "admin");
        await page.fill("form[name=loginForm] >> input[ng-model=password]", "password");
        await page.click("form[name=loginForm] >> .btn-primary");
    } else {
        console.log("Already in")
    }
}


export async function gotoOrgUnit(page, name) {
    await page.click("#navbar-org-units a");
    return await page.click(`waltz-entity-link >> '${name}'`);
}


export async function openSection(page, name) {
    return await page.click(`.wdsn-option >> '${name}'`);
}


export async function hoistSection(page, name) {
    await openSection(page, name);
    return await page.click(`waltz-icon[title='Embed section']`);
}