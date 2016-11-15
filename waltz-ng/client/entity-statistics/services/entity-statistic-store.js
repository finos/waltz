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
import {checkIsEntityRef} from "../../common/checks";


function store($http, BaseApiUrl) {
    const BASE = `${ BaseApiUrl }/entity-statistic`;

    const findAllActiveDefinitions = (options) => $http
        .get(`${ BASE }/definition`)
        .then(r => r.data);

    const findStatDefinition = (id) => $http
        .get(`${ BASE }/definition/${ id }`)
        .then(r => r.data);

    const findStatsForEntity = (entityRef) => {
        checkIsEntityRef(entityRef);

        return $http
            .get(`${ BASE }/${ entityRef.kind }/${ entityRef.id }`)
            .then(r => r.data);
    };

    const findStatValuesByIdSelector = (statId, options) => $http
        .post(`${ BASE }/value/${ statId }`, options)
        .then(r => r.data);

    const findRelatedStatDefinitions = (statId) => $http
        .get(`${ BASE }/definition/${ statId }/related`)
        .then(r => r.data);

    const findStatTallies = (statisticIds = [], selector) => {
        const options = {
            selector,
            statisticIds
        };

        return $http
            .post(`${ BASE }/tally`, options)
            .then(r => r.data);
    };

    const calculateStatTally = (definition, selector) => {
        return $http
            .post(`${ BASE }/tally/${ definition.id }/${ definition.rollupKind }`, selector)
            .then(r => r.data);
    };

    const calculateHistoricStatTally = (definition, selector, duration = 'MONTH') => {
        return $http
            .post(
                `${ BASE }/tally/historic/${ definition.id }/${ definition.rollupKind }`,
                selector,
                { params: { duration } })
            .then(r => r.data);
    };

    return {
        findAllActiveDefinitions,
        findStatDefinition,
        findStatsForEntity,
        findStatValuesByIdSelector,
        findRelatedStatDefinitions,
        findStatTallies,
        calculateStatTally,
        calculateHistoricStatTally
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;
