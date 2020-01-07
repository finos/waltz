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


/**
 * Given an array of objects and a groupingSelector will return an array of values along with their counts
 * in the original data.  The grouping selector can be the string name of a field in the data or a function.
 * @param data
 * @param groupingSelector
 * @returns {*}
 */
export function tallyBy(data = [], groupingSelector = "key") {
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
export function buildPropertySummer(countKey = "directCount",
                                    totalKey = "totalCount",
                                    childKey = "indirectCount") {
    const summer = (node) => {
        if (node == null) {
            return 0;
        }

        const directCount = Number(node[countKey] || 0);
        const sum = _.sumBy(node.children, summer); // recursive step

        const cumulativeTotal = sum + directCount;
        if (node.children) {
            node[totalKey] = cumulativeTotal;
            node[childKey] = sum;
        }

        return cumulativeTotal;
    };
    return summer;
}