/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
