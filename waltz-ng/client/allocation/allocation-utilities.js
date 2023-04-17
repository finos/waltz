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


export function calcWorkingTotal(enrichedAllocations = []) {
    return _.sumBy(
        enrichedAllocations,
        d => _.result(d, ["working", "percentage"], () => console.warn("Allocation has no working percentage, defaulting to 0",d) || 0));
}


export function validateItems(items = []) {
    const dirtyItems =  _.filter(items, d => d.working.dirty);
    if (_.isEmpty(items) && !_.isEmpty(dirtyItems)) {
        // nothing to check
        return true;
    }

    updateValidations(items);

    const saveEnabled = _.every(items, d => d.working.status !== "FAIL");
    return saveEnabled;
}


function updateValidations(items = []) {
    const totalFixed = _.sumBy(items, fa => fa.working.percentage);
    _.each(items, d => updateValidationForItem(d, totalFixed));
}


function updateValidationForItem(d, totalFixed) {
    if (d.working.percentage > 100) {
        d.working.status = "FAIL";
        d.working.message = "Cannot exceed 100%";
    } else if (d.working.percentage < 0) {
        d.working.status = "FAIL";
        d.working.message = "Cannot be less than 0%";
    } else if (totalFixed > 100) {
        d.working.status = "FAIL";
        d.working.message = "Total exceeds 100%";
    } else if (totalFixed < 100) {
        d.working.status = "WARN";
        d.working.message = "Total does not make 100%";
    } else {
        d.working.status = "OK";
        d.working.message = "";
    }
}

function updateDirtyFlag(item) {
    const changeType = determineChangeType(item);
    item.working.dirty = ! _.isEqual("NONE", changeType);
    return item;
}


export function updateDirtyFlags(items = []) {
    _.forEach(items, d => {
        updateDirtyFlag(d);
    });
    return _.some(items, d => d.working.dirty);
}


export function determineChangeType(d) {
    const currentlyAllocated = !_.isNil(d.allocation);
    const workingPercentage = _.get(d, ["working", "percentage"], 0);
    const currentPercentage = _.get(d, ["allocation", "percentage"], 0);

    if (!currentlyAllocated && d.working.isAllocated) {
        return "ADD";
    } else if (currentlyAllocated && !d.working.isAllocated) {
        return "REMOVE";
    } else if (workingPercentage !== currentPercentage) {
        return "UPDATE";
    } else {
        return "NONE";
    }
}