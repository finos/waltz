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

export default class BaseLookupService {

    constructor() {
        this.lookupsByType = {};
    }


    register(type, lookupMap) {
        const paramsOkay = _.isString(type) && _.isObject(lookupMap);

        if (!paramsOkay) {
            throw Error('Cannot register type and lookupMap unless they are a string and a map', type, lookupMap);
        }

        const existing = this.lookupsByType[type] || {};

        this.lookupsByType[type] = Object.assign({}, existing, lookupMap);

        return this;
    }


    lookup(type, value) {
        const lookupMap = this.lookupsByType[type];
        if (!lookupMap) {
            console.warn('No lookupMap registered for type', type);
            return '??' + value + '??';
        }
        return lookupMap[value] || '';
    }


    getAllByType(type) {
        return this.lookupsByType[type];
    }


    // @deprecated
    toOptions(type) {
        return _.chain(this.lookupsByType[type])
            .map((v, k) => ({ name: v, code: k}))
            .sortBy(o => o.name)
            .value();
    }

    // @deprecated
    toGridOptions(type) {
        return _.map(this.lookupsByType[type], (v, k) => ({ label: v, value: k}));
    }
}
