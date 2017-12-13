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

export function talliesById(tallies) {
    return _.reduce(
        tallies,
        (acc, tally) => {
            acc[tally.id] = tally.count;
            return acc;
        },
        {});
}

/**
 * Given an array of objects and a groupingSelector will return an array of values along with their counts
 * in the original data.  The grouping selector can be the string name of a field in the data or a function.
 * @param data
 * @param groupingSelector
 * @returns {*}
 */
export function tallyBy(data = [], groupingSelector = 'key') {
    return _.chain(data)
        .countBy(groupingSelector)
        .map((v, k) => ({ key: k, count: v }))
        .value()
}


/**
 * Given a node with a @countKey, adds total and child counts for the node.
 * Then recurses down children doing the same.
 * @param countKey
 * @param totalKey
 * @param childKey
 * @returns {function(*)}
 */
export function buildPropertySummer (countKey = 'directCount',
                                     totalKey = 'totalCount',
                                     childKey = 'indirectCount') {
    const summer = (node) => {
        if (node == null) {
            return 0;
        }

        const count = Number(node[countKey] || 0);
        const sum = _.sumBy(node.children, summer); // recursive step

        if (node.children) {
            node[totalKey] = sum + count;
            node[childKey] = sum;
        }

        return count + sum;
    };
    return summer;
};