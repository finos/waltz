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
 * Takes a predicate and returns a new function that can be used
 * as part of a reject step in a lodash chain.
 * The returned function has a property called `.results` which contains
 * all values which pass the predicate condition.
 *
 * @param predicate
 * @returns {function(*=): boolean}
 */
export function mkSiphon(predicate) {

    if (_.isNil(predicate)){
        throw "Cannot create a siphon without a predicate";
    }

    if (!_.isFunction(predicate)){
        throw "Must be given a predicate that is a function";
    }

    const siphon = (d) => {
        if (predicate(d)){
            siphon.results.push(d);
            return true;
        } else {
            return false;
        }
    }

    siphon.results = [];
    siphon.hasResults = () => !_.isEmpty(siphon.results);

    return siphon;
}
