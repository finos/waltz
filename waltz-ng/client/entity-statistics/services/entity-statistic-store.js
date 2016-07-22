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
import _ from "lodash";


function extractDefinitionIdsFromImmediateHierarchy(hierarchy) {
    const definitions = [
        hierarchy.self,
        hierarchy.parent,
        ...hierarchy.siblings,
        ...hierarchy.children
    ];

    return _.chain(definitions)
        .filter(s => s != null)
        .map('id')
        .value();
}


function store($http, BaseApiUrl) {
    const BASE = `${BaseApiUrl}/entity-statistic`;

    const findSummaryStatsByIdSelector = (options) => $http
        .post(`${BASE}/summary`, options)
        .then(r => r.data);

    const findStatValuesByIdSelector = (statId, options) => $http
        .post(`${BASE}/value/${statId}`, options)
        .then(r => r.data);

    const findRelatedStatDefinitions = (statId) => $http
        .get(`${BASE}/definition/${statId}/related`)
        .then(r => r.data);

    const findRelatedStatSummaries = (statId, options) => $http
        .post(`${BASE}/summary/${statId}/related`, options)
        .then(r => r.data);

    const findStatTallies = (definitions, selector) => {

        const statisticIds = _.isArray(definitions)
            ? definitions
            : extractDefinitionIdsFromImmediateHierarchy(definitions);

        const options = {
            selector,
            statisticIds
        };

        return $http
            .post(`${BASE}/tally`, options)
            .then(r => r.data);
    };

    return {
        findSummaryStatsByIdSelector,
        findStatValuesByIdSelector,
        findRelatedStatDefinitions,
        findRelatedStatSummaries,
        findStatTallies
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;
