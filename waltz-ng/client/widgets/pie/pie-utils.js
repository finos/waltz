/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
            count : _.sumBy(overspill, "count") + 1000
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


