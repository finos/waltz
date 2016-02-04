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

        this.lookupsByType[type] = {...existing, ...lookupMap};
    }


    lookup(type, value) {
        const lookupMap = this.lookupsByType[type];
        if (!lookupMap) {
            console.error('No lookupMap registered for type', type);
            return '??' + value + '??';
        }
        return lookupMap[value] || '';
    }


    getAllByType(type) {
        return this.lookupsByType[type];
    }


    toOptions(type) {
        return _.map(this.lookupsByType[type], (v, k) => ({ name: v, code: k}));
    }

    toGridOptions(type) {
        return _.map(this.lookupsByType[type], (v, k) => ({ label: v, value: k}));
    }
}
