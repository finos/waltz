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

import {checkIsEntityRef} from "../../common/checks";


function store($http, BaseApiUrl) {
    const BASE = `${ BaseApiUrl }/entity-statistic`;

    const findAllActiveDefinitions = () => $http
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

    const findStatAppsByIdSelector = (statId, options) => $http
        .post(`${ BASE }/app/${ statId }`, options)
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
        findStatAppsByIdSelector,
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


const serviceName = 'EntityStatisticStore';


export const EntityStatisticStore_API = {
    findAllActiveDefinitions: {
        serviceName,
        serviceFnName: 'findAllActiveDefinitions',
        description: 'finds all active entity statistic definitions'
    },
    findStatDefinition: {
        serviceName,
        serviceFnName: 'findStatDefinition',
        description: 'finds entity statistic definition for a given id'
    },
    findStatsForEntity: {
        serviceName,
        serviceFnName: 'findStatsForEntity',
        description: 'finds entity statistic for a given entity reference'
    },
    findStatValuesByIdSelector: {
        serviceName,
        serviceFnName: 'findStatValuesByIdSelector',
        description: 'finds entity statistic values by app id selector'
    },
    findStatAppsByIdSelector: {
        serviceName,
        serviceFnName: 'findStatAppsByIdSelector',
        description: 'finds entity statistic apps by app id selector'
    },
    findRelatedStatDefinitions: {
        serviceName,
        serviceFnName: 'findRelatedStatDefinitions',
        description: 'finds related entity statistic definitions for a given stat id'
    },
    findStatTallies: {
        serviceName,
        serviceFnName: 'findStatTallies',
        description: 'finds entity statistic tallies for a list of stat ids and an app id selector'
    },
    calculateStatTally: {
        serviceName,
        serviceFnName: 'calculateStatTally',
        description: 'calculates entity statistic tallies for a statistic definition and app id selector'
    },
    calculateHistoricStatTally: {
        serviceName,
        serviceFnName: 'calculateHistoricStatTally',
        description: 'calculates historic entity statistic tallies for a statistic definition and app id selector'
    }
};


export default {
    store,
    serviceName
};

