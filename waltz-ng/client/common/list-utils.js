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
 * @param xs
 * @returns {*}
 */
export function toCumulativeCounts(xs) {
    return _.reduce(
        xs,
        (acc, x) => {
            const last = _.last(acc) || 0;
            acc.push(last + x);
            return acc;
        },
        []);
}
