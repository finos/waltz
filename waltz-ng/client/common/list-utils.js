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
 *
 * @param xs
 * @param ys
 * @returns boolean - `true` iff all elements of `ys` occur in `xs`
 */
export function containsAll(xs = [], ys = []) {
    return _.every(ys, y => _.includes(xs, y));
}


/**
 *
 * @param xs
 * @param ys
 * @returns boolean - `true` iff all elements of `ys` occur in `xs`, works for objects
 */
export function containsAny(xs = [], ys = []) {
    return _.some(ys, y => _.some(xs, x => _.isEqual(y, x)));
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
    for (let i = 0; i < entries.length; i += chunkSize) {
        chunkedEntries.push(entries.slice(i, i + chunkSize));
    }
    return chunkedEntries;
}


export function reverse(array = []) {
    const newArray = _.clone(array);
    return newArray.reverse();
}

export function coalesce(...xss) {
    return _.find(xss, xs => !_.isEmpty(xs));
}

/**
 * returns the index of the previous item in the list.
 * If the current index is the first item then will cycle back to last item in the list.
 * @param list
 * @param currentIndex
 * @returns {number}
 */
export function determineIndexOfPreviousItemInList(list, currentIndex) {
    if (currentIndex === 0) {
        return _.size(list) - 1;
    } else {
        return currentIndex - 1;
    }
}

/**
 * returns the index of the next item in the list.
 * If the current index is the final item then will cycle back to first item in the list.
 * @param list
 * @param currentIndex
 * @returns {number}
 */
export function determineIndexOfNextItemInList(list, currentIndex) {
    if (currentIndex === _.size(list) - 1) {
        return 0;
    } else {
        return currentIndex + 1;
    }
}