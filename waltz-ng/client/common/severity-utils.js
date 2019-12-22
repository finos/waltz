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

/**
 * @typedef {Object} SeverityInfo
 * @property {string} bootstrapClass The bootstrap class name e.g. `warning`, `danger`
 * @property {number} score Number used to rank the severities (higher is more severe)
 */


const severityMap = {
    "INFORMATION": {
        bootstrapClass: "success",
        score: 0
    },
    "WARNING": {
        bootstrapClass: "warning",
        score: 1
    },
    "ERROR": {
        bootstrapClass: "danger",
        score: 2
    }
};


const defaultSeverity = severityMap.INFORMATION;


/**
 * @param {String} severity Severity name, e.g. `INFORMATION`, `WARNING`
 * @returns {SeverityInfo} the associated Severity Info definition or a default if not found
 */
function lookupBySeverity(severity) {
    return _.get(severityMap, [ severity ], defaultSeverity);
}

/**
 * Given a severity from the `com.khartec.waltz.model.Severity`
 * enum will return it's corresponding bootstrap classname
 * @param severity
 * @returns {string}
 */
export function severityToBootstrapAlertClass(severity) {
    return lookupBySeverity(severity).bootstrapClass;
}


export function severityToBootstrapBtnClass(severity) {
    return `btn-${lookupBySeverity(severity).bootstrapClass}`;
}


export function findHighestSeverity(severities = []) {
    return _
        .maxBy(
            severities,
            s => _.get(severityMap, [s], defaultSeverity).score);
}


/**
 * Given a list of severities, will find highest and return its associated
 * bootstrap button class name.
 *
 * @param {string[]} severities
 * @returns {string} bootstrap button classname
 */
export function determineColorOfSubmitButton(severities = []) {
    return severityToBootstrapBtnClass(
        findHighestSeverity(severities));
}

