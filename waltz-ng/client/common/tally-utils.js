/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
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
 * Given a node with a @countKey, adds total and child counts for the node
 * @param countKey
 * @param totalKey
 * @param childKey
 * @returns {function(*)}
 */
export function buildPropertySummer (countKey, totalKey, childKey) {
    const summer = (node) => {
        if (node == null) {
            return 0;
        }

        const count = Number(node[countKey] || 0);
        const sum = _.sumBy(node.children, summer);

        if (node.children) {
            node[totalKey] = sum + count;
            node[childKey] = sum;
        }

        return count + sum;
    };
    return summer;
};