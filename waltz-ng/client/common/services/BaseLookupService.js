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

export default class BaseLookupService {

    constructor() {
        this.lookupsByType = {};
    }


    register(type, lookupMap) {
        const paramsOkay = _.isString(type) && _.isObject(lookupMap);

        if (!paramsOkay) {
            throw Error("Cannot register type and lookupMap unless they are a string and a map", type, lookupMap);
        }

        const existing = this.lookupsByType[type] || {};

        this.lookupsByType[type] = Object.assign({}, existing, lookupMap);

        return this;
    }


    lookup(type, value, defaultValue = "") {
        const lookupMap = this.lookupsByType[type];
        if (!lookupMap) {
            console.warn("No lookupMap registered for type", type);
            return "??" + value + "??";
        }
        return lookupMap[value] || defaultValue;
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
