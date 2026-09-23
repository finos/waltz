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

import {assert} from "chai";
import {
    SHEETS,
    safeSheetName,
    buildSheetData
} from "../../../client/system/svelte/analytics-dashboard/analytics-export-mappings";


describe("system/analytics-dashboard/analytics-export-mappings", () => {

    const datasets = {
        accessLogTrends: [{period: "2024-03", counts: 10, distinctUserCount: 3}],
        activityByHour: [{hour: 9, counts: 5}],
        activityByDay: [{dayOfWeek: 1, counts: 7}],
        topPages: [{state: "main.app.view", counts: 9}],
        topUsers: [{userId: "admin", counts: 12}],
        sessionDurations: [{userId: "admin", period: "2024-03-01", sessionDuration: 200, counts: 4}],
        changeLogTrends: {"2024-03": {counts: 20, distinctUserCount: 6}},
        topContributors: {admin: 15},
        changesByOperation: {ADD: 8},
        changesBySeverity: {INFORMATION: 30},
        changesByEntityKind: {APPLICATION: 25},
        changesByChildKind: {PERSON: 4},
        operationTrends: {ADD: {"2024-03": 5, "2024-04": 6}},
        changesByDay: {"1": 11, "7": 2}
    };

    const rowsFor = (name, ds = datasets) => buildSheetData(ds).find(s => s.name === name).rows;

    describe("safeSheetName", () => {
        it("replaces characters Excel forbids in sheet names", () =>
            assert.equal("a b c d e", safeSheetName("a:b*c/d?e")));
        it("truncates to Excel's 31 char limit", () =>
            assert.equal(31, safeSheetName("x".repeat(40)).length));
    });

    describe("buildSheetData", () => {
        it("produces one sheet per chart in dashboard order", () => {
            const names = buildSheetData(datasets).map(s => s.name);
            assert.equal(SHEETS.length, names.length);
            assert.equal("Access Log Trends", names[0]);
            assert.equal("Changes by Day", names[names.length - 1]);
        });

        it("projects access-log trends to friendly columns", () =>
            assert.deepEqual([{Period: "2024-03", Hits: 10, "Distinct Users": 3}], rowsFor("Access Log Trends")));

        it("renders day-of-week numbers as day names", () => {
            assert.deepEqual([{Day: "Monday", Hits: 7}], rowsFor("Access by Day"));
            assert.deepEqual([{Day: "Monday", Changes: 11}, {Day: "Sunday", Changes: 2}], rowsFor("Changes by Day"));
        });

        it("surfaces change-log trend counts and distinct users (not the nested object)", () =>
            assert.deepEqual([{Period: "2024-03", Changes: 20, "Distinct Users": 6}], rowsFor("Change Log Trends")));

        it("flattens operation trends to one row per operation+period", () =>
            assert.deepEqual(
                [{Operation: "ADD", Period: "2024-03", Changes: 5}, {Operation: "ADD", Period: "2024-04", Changes: 6}],
                rowsFor("Operation Trends")));

        it("maps simple count maps to key/value columns", () => {
            assert.deepEqual([{Operation: "ADD", Changes: 8}], rowsFor("Changes by Operation"));
            assert.deepEqual([{"Entity Kind": "APPLICATION", Changes: 25}], rowsFor("Changes by Entity Kind"));
        });

        it("yields empty rows for missing datasets", () =>
            assert.deepEqual([], rowsFor("Top Pages", {})));
    });
});
