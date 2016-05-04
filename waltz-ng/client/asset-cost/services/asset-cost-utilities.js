/*
 *  This file is part of Waltz.
 *
 *  Waltz is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Waltz is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import _ from "lodash";
import {numberFormatter} from "../../common";


export function categorizeCostsIntoBuckets(costs) {

    // bucketPredicate:: bucket -> amount -> bool
    const bucketPredicate = b => a => (a >= b.min) && (a < b.max);

    const buckets = [
        { min: 0, max: 1000, name: '€0 < 1K', idx: 0, size: 0},
        { min: 1000, max: 5000, name: '€1K < 5K', idx: 1, size: 0},
        { min: 5000, max: 10000, name: '€5K < 10K', idx: 2, size: 0},
        { min: 10000, max: 50000, name: '€10K < 50K', idx: 3, size: 0},
        { min: 50000, max: 100000, name: '€50K < 100K', idx: 4, size: 0},
        { min: 100000, max: 500000, name: '€100K < 500K', idx: 5, size: 0},
        { min: 500000, max: 1000000, name: '€500K < 1M', idx: 6, size: 0},
        { min: 1000000, max: Number.MAX_VALUE, name: '€1M +', idx: 7, size: 0}
    ];

    const findBucket = (c) => {
        return _.find(buckets, b => bucketPredicate(b)(c));
    };


    _.each(costs, c => {
        const bucket = findBucket(c.cost.amount);
        if (bucket) {
            bucket.size++;
        } else {
            console.log('failed to find bucket for ', c);
        }
    });

    return buckets;
}


/**
 * Given an array of cost data will return
 * a formatted total cost for the portfolio
 * @param costs
 */
export function calcPortfolioCost(costs) {
    if (!costs || !costs.totalCost) return;

    return '€ ' + numberFormatter(costs.totalCost.amount, 1);
}
