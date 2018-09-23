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