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

import _ from 'lodash';


export function calcTotal(data) {
    return _.sumBy(data, 'count')
}


export function isPieEmpty(data) {
    return calcTotal(data) === 0;
}



export function limitSegments(data = [], maxSegments = 5) {
    const removeEmptySegments = segs => _.filter(segs, s => s.count !== 0)
    if (data.length > maxSegments) {
        const sorted = _.sortBy(data, d => d.count * -1);
        const topData = _.take(sorted, maxSegments);
        const otherData = _.drop(sorted, maxSegments);
        const otherDatum = {
            key: 'Other',
            count : _.sumBy(otherData, "count")
        };

        return removeEmptySegments(_.concat(topData, otherDatum));
    } else {
        return removeEmptySegments(data);
    }
}


