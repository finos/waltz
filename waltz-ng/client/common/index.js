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
import {nest} from "d3-collection";

export {invokeFunction} from "./function-utils";


export const formats = {
    daysAndMinutes: "ddd Do MMM YYYY - HH:mm:ss",
    daysOnly: "ddd Do MMM YYYY",
    parse: "YYYY-MM-DDThh:mm:ss.SSS",
    parseDateOnly: "YYYY-MM-DD"
};


export const entityLifecycleStatuses = {
    ACTIVE: "ACTIVE",
    PENDING: "PENDING",
    REMOVED: "REMOVED"
};

export const allEntityLifecycleStatuses = Object.values(entityLifecycleStatuses);

export function notEmpty(xs) {
    return ! _.isEmpty(xs);
}


export function isEmpty(xs) {
    return _.isEmpty(xs);
}


export function randomPick(xs) {
    if (!xs) throw new Error("Cannot pick from a null set of options");

    const choiceCount = xs.length - 1;
    const idx = Math.round(Math.random() * choiceCount);
    return xs[idx];
}


export function simpleTermSearch(data = [], qry) {
    if (_.isEmpty(qry)) return data;
    if (_.isEmpty(data)) return [];
    const terms = _.map(_.split(qry, " "), t => t.toLowerCase());

    return _.filter(
        data,
        d => !_.isNil(d) && _.every(terms, t => d.toLowerCase().indexOf(t) > -1));
}


/**
 *
 * @param items - items to be searched
 * @param searchStr - query string to search for (case insensitive)
 * @param searchFields - fields in the items to consider when searching, may be a function
 * @param omitCallback - evaluated against every passing results to see if it should be omitted, defaults to false
 * @returns {Array}
 */
export function termSearch(items = [],
                           searchStr = "",
                           searchFields = [],
                           omitCallback = () => false) {
    if (_.isEmpty(searchStr)) {
        return _.reject(items, omitCallback);
    }

    const terms = searchStr.toLowerCase().split(/\W/);

    const getSearchFieldsForItem = item => _.isEmpty(searchFields)
        ? _.chain(item)
            .keys()
            .reject(field => field.startsWith("$") || _.isFunction(_.get(item, field)))
            .value()
        : searchFields;

    return _
        .chain(items)
        .filter(item => {
            const targetStr = _
                .chain(getSearchFieldsForItem(item))
                .map(field => _.isFunction(field)
                    ? field(item)
                    : _.get(item, field, ""))
                .map(v => String(v).toLowerCase())
                .join(" ")
                .value()
                .toLowerCase();

            return _.every(terms, term => targetStr.includes(term));
        })
        .reject(omitCallback)
        .value();
}


/**
 * the d3 nest function aggregates using the property name 'values', this
 * function creates a copy of the data with the name 'count'.
 *
 * @param data
 * @returns {Array|*}
 */
function toCountData(data = []) {
    return _.map(
        data,
        d => ({
            key: d.key,
            count: d.value
        }));
}


export function toKeyCounts(items = [], keyFn = x => x) {
    if (! items) return [];
    return toCountData(nest()
        .key(keyFn)
        .rollup(d => d.length)
        .entries(items));
}


export function resetData(vm, initData = {}) {
    return Object.assign(vm, _.cloneDeep(initData));
}


/**
 * Deep copies `initData` into `vm`
 * @param vm
 * @param initData
 * @returns {*} - `vm` enriched with `initData`
 */
export function initialiseData(vm, initData) {
    return _.defaultsDeep(vm, _.cloneDeep(initData));
}


const getElemHierarchy = (elem) => {
    // Set up a parent array
    const parents = [elem];
    // Push each parent element to the array
    for ( ; elem && elem !== document; elem = elem.parentNode ) {
        parents.push(elem);
    }
    // Return our parent array
    return parents;
};


const getWaltzTagNames = (elem) => {
    if (! elem) {
        const msg = "Usage: Inspect an item on the page. "
            + "From the elements view, select 'store as global variable'. "
            + "This will create a temp variable, use this to call this method e.g. getWaltzTagNames(temp1)";
        console.log(msg);
        return [];
    }
    return _
        .chain(getElemHierarchy(elem))
        .map(e => e.localName)
        .filter(n => n.startsWith("waltz"))
        .value();
};

global.getWaltzTagNames = getWaltzTagNames;
