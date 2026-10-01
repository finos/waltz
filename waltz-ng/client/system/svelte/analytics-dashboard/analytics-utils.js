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

import moment from "moment";
import {period} from "../../../common/services/enums/period";

const MONTH_NAMES = ["Jan", "Feb", "Mar", "Apr", "May", "Jun",
                     "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];


/**
 * Format a backend period key for display. `period` matches the backend Duration enum
 * (DAY/WEEK/MONTH/YEAR), compared case-insensitively. Backend period keys look like
 * "2024-03-10" (day), "2024-W12" (iso week), "2024-03" (month) and "2024" (year).
 */
export function formatPeriodLabel(key, period) {
    const p = (period || "").toLowerCase();
    if (p === "week") {
        if (key.includes("-W")) {
            const [year, week] = key.split("-W");
            return `Week ${parseInt(week)}, ${year}`;
        }
        return `Week ${key}`;
    } else if (p === "month") {
        if (key.includes("-")) {
            const [year, month] = key.split("-");
            return `${MONTH_NAMES[parseInt(month) - 1]} ${year}`;
        }
        return key;
    } else if (p === "day") {
        if (key.includes("-")) {
            // Build a local date from the parts; `new Date("2024-03-10")` parses as UTC
            // and renders a day early in negative-offset timezones.
            const [year, month, day] = key.split("-").map(Number);
            return new Date(year, month - 1, day).toLocaleDateString();
        }
        return key;
    } else if (p === "year") {
        return key;
    }
    return key;
}


/**
 * Pick a sensible calendar bucket for a date range so charts stay readable at any span.
 * Returns a `period` enum key (DAY/WEEK/MONTH/YEAR).
 */
export function deriveGrouping(startDate, endDate) {
    const days = moment(endDate).diff(moment(startDate), "days");
    if (days <= 31) return period.DAY.key;
    if (days <= 120) return period.WEEK.key;
    if (days <= 750) return period.MONTH.key;
    return period.YEAR.key;
}


/**
 * Turn an enum-style value ("LOGICAL_DATA_FLOW") into a readable label ("Logical Data Flow").
 */
export function formatEnumName(value) {
    if (!value) return "Unknown";
    return value.split("_")
        .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
        .join(" ");
}


/**
 * Capitalise a single-word operation code ("ADD" -> "Add").
 */
export function formatOperation(operation) {
    if (!operation) return "Unknown";
    return operation.charAt(0).toUpperCase() + operation.slice(1).toLowerCase();
}
