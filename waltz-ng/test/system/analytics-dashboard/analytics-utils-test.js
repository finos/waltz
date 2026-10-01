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
import moment from "moment";
import {period} from "../../../client/common/services/enums/period";
import {
    formatPeriodLabel,
    deriveGrouping,
    formatEnumName,
    formatOperation
} from "../../../client/system/svelte/analytics-dashboard/analytics-utils";


describe("system/analytics-dashboard/analytics-utils", () => {

    describe("formatPeriodLabel", () => {
        it("formats a month key", () => assert.equal("Mar 2024", formatPeriodLabel("2024-03", "month")));
        it("formats an iso-week key", () => assert.equal("Week 12, 2024", formatPeriodLabel("2024-W12", "week")));
        it("returns the year key unchanged", () => assert.equal("2024", formatPeriodLabel("2024", "year")));
        it("renders day keys as a local locale date (no UTC off-by-one)", () =>
            assert.equal(new Date(2024, 2, 10).toLocaleDateString(), formatPeriodLabel("2024-03-10", "day")));
        it("is case-insensitive on the period", () => assert.equal("Mar 2024", formatPeriodLabel("2024-03", "MONTH")));
        it("falls back to the key for an unknown period", () => assert.equal("2024-03", formatPeriodLabel("2024-03", "decade")));
    });

    describe("deriveGrouping", () => {
        const base = "2020-01-01";
        const plus = (n) => moment(base).add(n, "days").format("YYYY-MM-DD");

        it("<= 31 days is daily", () => assert.equal(period.DAY.key, deriveGrouping(base, plus(31))));
        it("32..120 days is weekly", () => {
            assert.equal(period.WEEK.key, deriveGrouping(base, plus(32)));
            assert.equal(period.WEEK.key, deriveGrouping(base, plus(120)));
        });
        it("121..750 days is monthly", () => {
            assert.equal(period.MONTH.key, deriveGrouping(base, plus(121)));
            assert.equal(period.MONTH.key, deriveGrouping(base, plus(750)));
        });
        it("> 750 days is yearly", () => assert.equal(period.YEAR.key, deriveGrouping(base, plus(751))));
    });

    describe("formatEnumName", () => {
        it("title-cases an underscore-separated enum", () => assert.equal("Logical Data Flow", formatEnumName("LOGICAL_DATA_FLOW")));
        it("handles a single word", () => assert.equal("Add", formatEnumName("ADD")));
        it("returns Unknown for empty input", () => {
            assert.equal("Unknown", formatEnumName(null));
            assert.equal("Unknown", formatEnumName(""));
        });
    });

    describe("formatOperation", () => {
        it("capitalises the first letter only", () => assert.equal("Add", formatOperation("ADD")));
        it("handles lower case input", () => assert.equal("Update", formatOperation("update")));
        it("returns Unknown for empty input", () => assert.equal("Unknown", formatOperation(null)));
    });
});
