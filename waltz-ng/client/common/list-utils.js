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
 *
 * @param xs
 * @param ys
 * @returns boolean - `true` iff all elements of `ys` occur in `xs`
 */
export function containsAll(xs = [], ys = []) {
    return _.every(ys, y => _.includes(xs, y));
}


/**
 * given an array of numbers (xs) returns the cumulative counts. E.g.
 *
 * ```
 *   toCumulativeCounts([1,2,3]) => [1, 3, 6];
 * ```
 *
 * @param xs - array of integers
 * @returns [] - cumulative counts
 */
export function toCumulativeCounts(xs = []) {
    return _.reduce(
        xs,
        (acc, x) => {
            const last = _.last(acc) || 0;
            acc.push(last + x);
            return acc;
        },
        []);
}


/**
 * Given a list of items and a mechanism to extract a key this function
 * will return a map giving the offset in `arr` of a key.
 * @param arr
 * @param keyFn - defaults to `d => d.id`
 * @returns {*} - map of `result[key]` -> offset of key in arr
 */
export function toOffsetMap(arr = [], keyFn = d => d.id) {
    return _.reduce(arr, (acc, d, idx) => {
        acc[keyFn(d)] = idx;
        return acc;
    }, {});
}


/**
 * Moves the item at offset `idx` by `delta` positions in the given `array`.
 * Clamps to the start and end of the array.
 * @param array
 * @param idx
 * @param delta
 * @returns {*}
 */
export function move(array, idx, delta) {
    const copy = array.slice();

    if (delta === 0) {
        return copy;
    }

    const targetIdx = _.clamp(
        idx + delta,
        0,
         array.length);

    const itemToMove = array[idx];
    copy.splice(idx , 1);
    copy.splice(targetIdx, 0, itemToMove);

    return copy;
}

/**
 * given an array, breaks it into multiple arrays
 *
 * @param entries original array (eg: [e1, e2, e3, e4, e5])
 * @param chunkSize size of chunks (eg: 2)
 * @returns {Array} array of chunked arrays (eg: [[e1, e2], [e3, e4], [e5])
 */
export function mkChunks(entries = [], chunkSize) {
    const chunkedEntries = [];
    for (var i = 0; i < entries.length; i += chunkSize) {
        chunkedEntries.push(entries.slice(i, i + chunkSize));
    }
    return chunkedEntries;
}