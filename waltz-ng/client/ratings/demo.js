
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

import _ from 'lodash';

import { randomPick } from '../common';
import { calculateGroupSummary } from  './directives/common';


export const MEASURABLES = [
    {name: 'FIC', id: 1, kind: 'BUSINESS' },
    {name: 'FX', id: 2, kind: 'BUSINESS' },
    {name: 'Equities', id: 3, kind: 'BUSINESS' },
    {name: 'Commodities', id: 4, kind: 'BUSINESS' },
    {name: 'Cash Secs', id: 5, kind: 'BUSINESS' },
    {name: 'Loans and Mortgages', id: 6, kind: 'BUSINESS' }
];

const APPLICATIONS = [
    { name: 'Waltz', id: 1, kind: 'APPLICATION'},
    { name: 'TradeSys', id: 2, kind: 'APPLICATION'},
    { name: 'Dog', id: 692, kind: 'APPLICATION'},
    { name: 'A very long name indeed, perhaps a bit too long.  Though some would say just right', id: 4, kind: 'APPLICATION'},
    { name: 'elephant', id: 5, kind: 'APPLICATION'},
    { name: 'Jazz', id: 6, kind: 'APPLICATION'},
    { name: 'giraffe', id: 7, kind: 'APPLICATION'},
    { name: 'hawk', id: 8, kind: 'APPLICATION'},
    { name: 'iguana', id: 9, kind: 'APPLICATION' }
];


export function mkGroup(groupRef, numApps) {

    const rawData = _.take(APPLICATIONS, numApps)
        .map(app => ({
            ratings: _.map(MEASURABLES, () => ({ current: randomPick(['R', 'A', 'G', 'A', 'Z', 'Z', 'Z']) })),
            subject: app
        }));

    return {
        groupRef,
        measurables: MEASURABLES,
        subjects: _.take(APPLICATIONS, numApps),
        raw: rawData,
        summaries: calculateGroupSummary(rawData),
        collapsed: true
    };
}