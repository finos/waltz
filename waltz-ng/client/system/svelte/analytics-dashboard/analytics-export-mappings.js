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

// Pure (dependency-free) mapping of the analytics datasets onto spreadsheet rows.
// Kept separate from the xlsx/browser export so it can be unit tested in isolation.

const DAY_NAMES = {1: "Monday", 2: "Tuesday", 3: "Wednesday", 4: "Thursday", 5: "Friday", 6: "Saturday", 7: "Sunday"};
const dayName = (n) => DAY_NAMES[Number(n)] || n;

const arr = (d) => Array.isArray(d) ? d : [];
const entries = (d) => (d && typeof d === "object" && !Array.isArray(d)) ? Object.entries(d) : [];

// One worksheet per chart, in dashboard order. `toRows` maps a dataset onto tidy rows
// whose keys become the column headers, so each sheet only carries meaningful columns
// (the raw access-log endpoints share a 14-field DTO, most of it null per chart).
export const SHEETS = [
    {name: "Access Log Trends", key: "accessLogTrends", toRows: d => arr(d).map(x => ({Period: x.period, Hits: x.counts, "Distinct Users": x.distinctUserCount}))},
    {name: "Accesses by Hour", key: "activityByHour", toRows: d => arr(d).map(x => ({Hour: x.hour, Hits: x.counts}))},
    {name: "Access by Day", key: "activityByDay", toRows: d => arr(d).map(x => ({Day: dayName(x.dayOfWeek), Hits: x.counts}))},
    {name: "Top Pages", key: "topPages", toRows: d => arr(d).map(x => ({Page: x.state, Hits: x.counts}))},
    {name: "Top Active Users", key: "topUsers", toRows: d => arr(d).map(x => ({User: x.userId, Hits: x.counts}))},
    {name: "Session Durations", key: "sessionDurations", toRows: d => arr(d).map(x => ({User: x.userId, Date: x.period, "Session Duration": x.sessionDuration, Hits: x.counts}))},
    {name: "Change Log Trends", key: "changeLogTrends", toRows: d => entries(d).map(([k, v]) => ({Period: k, Changes: v?.counts, "Distinct Users": v?.distinctUserCount}))},
    {name: "Top Contributors", key: "topContributors", toRows: d => entries(d).map(([k, v]) => ({User: k, Changes: v}))},
    {name: "Changes by Operation", key: "changesByOperation", toRows: d => entries(d).map(([k, v]) => ({Operation: k, Changes: v}))},
    {name: "Changes by Severity", key: "changesBySeverity", toRows: d => entries(d).map(([k, v]) => ({Severity: k, Changes: v}))},
    {name: "Changes by Entity Kind", key: "changesByEntityKind", toRows: d => entries(d).map(([k, v]) => ({"Entity Kind": k, Changes: v}))},
    {name: "Changes by Child Kind", key: "changesByChildKind", toRows: d => entries(d).map(([k, v]) => ({"Child Kind": k, Changes: v}))},
    {name: "Operation Trends", key: "operationTrends", toRows: d => entries(d).flatMap(([op, byPeriod]) => entries(byPeriod).map(([p, c]) => ({Operation: op, Period: p, Changes: c})))},
    {name: "Changes by Day", key: "changesByDay", toRows: d => entries(d).map(([k, v]) => ({Day: dayName(k), Changes: v}))}
];


// Excel sheet names are limited to 31 chars and may not contain []:*?/\
export function safeSheetName(name) {
    return name.replace(/[\[\]:*?/\\]/g, " ").slice(0, 31);
}


// Build the ordered [{name, rows}] list for the workbook from the fetched datasets.
export function buildSheetData(datasets) {
    return SHEETS.map(({name, key, toRows}) => ({
        name: safeSheetName(name),
        rows: toRows(datasets[key]) || []
    }));
}
