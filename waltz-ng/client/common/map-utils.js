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
 * takes two maps of the form <code>{ x -> [ys] }</code> and returns
 * a new map with the merged results. When both input maps contain
 * the same key the associated lists are concatenated.
 *
 * Example:
 *
 * <code>
 * > mergeKeyedLists({a: [1], b: [2]}, {a: [3], c: [4]})
 *
 * { a: [1, 3], b: [2], c: [4] }
 * </code>
 */
export function mergeKeyedLists(m1 = {}, m2 = {}) {
    const merged = _.mergeWith(m1, m2, _.union);
    return merged;
}


/**
 * take an array and groups the items according to the
 * result of invoking the <code>keyFn</code> on each item.
 * Values are transformed using an (optional) <code>valFn</code>.
 *
 * Example:
 *
 * <code>
 * > toGroupedMap( [{k: 1, v: 'a'}, {k: 2, v: 'b'}, { k: 1, v: 'c'}], d => d.k, d => d.v);
 *
 * { a: ['a', 'c'], 2: ['b'] }
 * </code>
 *
 * @param xs
 * @param keyFn
 * @param valFn
 */
export function toGroupedMap(xs = [], keyFn, valFn = _.identity) {
    const reducer = (acc, x) => {
        const k = keyFn(x);
        const v = valFn(x);
        const bucket = acc[k] || [];
        acc[k] = bucket.concat([v]);
        return acc;
    };
    return _.reduce(xs, reducer, {});
}


export function toMap(xs, keyFn, valFn = _.identity) {
    const reducer = (acc, x) => {
        const k = keyFn(x);
        acc[k] = valFn(x);
        return acc;
    };
    return _.reduce(xs, reducer, {});
}

/**
 * Takes an array and groups by a key extraction function, processing values
 * with a value transformation function.
 * @param coll
 * @param keyFn
 * @param valFn
 */
export function groupAndMap(coll = [], keyFn = d => d.id, valFn = d => d) {
    return _.reduce(
        coll,
        (acc, x) => {
            const k = keyFn(x);
            const bucket = acc[k] || [];
            bucket.push(valFn(x));
            acc[k] = bucket;
            return acc;
        },
        {});
}

