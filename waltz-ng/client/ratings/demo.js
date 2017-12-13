
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