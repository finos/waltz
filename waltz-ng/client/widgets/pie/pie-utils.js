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

import _ from "lodash";


export function calcTotal(data) {
    return _.sumBy(data, "count")
}


export function isPieEmpty(data) {
    return calcTotal(data) === 0;
}


const MAX_PIE_SEGMENTS = 6;


/**
 * Takes a list of elements which match: `{ ... count: <number> ... }`
 * and returns an object with 3 parts.
 *
 * - The `primary` list is consist of the highest ranked elements (cutoff at `maxSegments`),
 * - the `overspill` is those elements after the `maxSegments` cutoff,
 * - the `overspillSummary` is a sum of the `overspill`
 *
 * @param data
 * @param maxSegments
 * @returns {*}  `{ primary: [], overspill: [], overspillSummary: {} }`
 */
export function toSegments(data = [], maxSegments = MAX_PIE_SEGMENTS) {
    const orderedData = _
        .chain(data)
        .filter(d => d.count !== 0)
        .sortBy(d => d.count * -1)
        .value();

    if (orderedData.length > maxSegments) {
        const overspill = _.drop(orderedData, (maxSegments - 1));
        const primary = _.take(orderedData, (maxSegments - 1));
        const overspillSummary = {
            key: "Other",
            isOverspillSummary: true,
            count : _.sumBy(overspill, "count")
        };
        return {
            primary,
            overspill,
            overspillSummary
        };
    } else {
        return {
            primary: orderedData,
            overspill: [],
            overspillSummary: null
        };
    }
}


