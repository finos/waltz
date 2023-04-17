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


export function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/legal-entity-relationship-kind`;


    const findAll = () =>
        $http.get(BASE)
            .then(result => result.data);

    const getById = (id) =>
        $http.get(`${BASE}/id/${id}`)
            .then(result => result.data);

    const getUsageStatsForKindAndSelector = (id, selector) => {
        return $http
            .post(`${BASE}/stats/relationship-kind/${id}/selector`, selector)
            .then(result => result.data)
    };

    return {
        findAll,
        getById,
        getUsageStatsForKindAndSelector
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


export const serviceName = "LegalEntityRelationshipKindStore";


export const LegalEntityRelationshipKindStore_API = {
    findAll: {
        serviceName,
        serviceFnName: "findAll",
        description: "executes findAll"
    },
    getById: {
        serviceName,
        serviceFnName: "getById",
        description: "executes getById"
    },
    getUsageStatsForKindAndSelector: {
        serviceName,
        serviceFnName: "getUsageStatsForKindAndSelector",
        description: "executes getUsageStatsForKindAndSelector"
    }
};


export default {
    serviceName,
    store
};
