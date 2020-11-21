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


/**
 * Attempts to login to waltz using the default admin
 * credentials.  If the user is already logged in
 * this function will simpy return.
 *
 * @param page
 * @returns {Promise<void>}
 */
export async function login(page) {
    console.log("Login");
    const menuText = await page.textContent(".waltz-navbar-profile .dropdown-toggle"); // span");
    console.log("MT", menuText)
    if (menuText.indexOf("Profile") === -1) {
        console.log("Login required");
        await page.click(".waltz-navbar-profile");
        await page.click(".waltz-navbar-profile >> 'Login'");
        await page.waitForSelector("form[name=loginForm]");
        await page.fill("form[name=loginForm] >> input[ng-model=username]", "admin");
        await page.fill("form[name=loginForm] >> input[ng-model=password]", "password");
        await page.click("form[name=loginForm] >> .btn-primary");
    } else {
        console.log("Login not required");
    }
}



export async function beginWithLogin(page) {
    await page.goto("http://localhost:8000/home");
    await page.waitForTimeout(400);
    await login(page);
    await page.waitForTimeout(200);
}

/**
 * Opens an org unit page by navigating to the org unit
 * list page and selecting the named org unit from the
 * tree control
 *
 * @param page
 * @param name
 * @returns {Promise<*>}
 */
export async function openOrgUnitViaList(page, name) {
    console.log("Open Org Unit via list", name);
    await page.click("#navbar-org-units a");
    return await page.click(`waltz-entity-link >> '${name}'`);
}


/**
 * Opens the named section
 *
 * @param page
 * @param name
 * @returns {Promise<*>}
 */
export async function openSection(page, name) {
    console.log("Open section", name);
    return await page.click(`.wdsn-option >> '${name}'`);
}


/**
 * Opens the named section then clicks the embed link
 * to 'hoist' the section onto it's own page
 *
 * @param page
 * @param name
 * @returns {Promise<*>}
 */
export async function hoistSection(page, name) {
    console.log("Hoisting section", name);
    await openSection(page, name);
    return await page.click(`waltz-icon[title='Embed section']`);
}


/**
 * Opens the search section, enters the application name
 * and then clicks on the name when it appears in the search result.
 *
 * @param page
 * @param name
 * @returns {Promise<*>}
 */
export async function openApplicationViaSearch(page, name) {
    console.log("Opening application via search", name)
    await page.click(".navbar-search a");
    await page.fill(".wnso-search-region >> input", name);
    return await page.click(`'${name}'`);
}