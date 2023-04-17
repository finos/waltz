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

    const BASE = `${BaseApiUrl}/legal-entity-relationship`;


    const findByLegalEntityId = (id) =>
        $http
            .get(`${BASE}/legal-entity-id/${id}`)
            .then(result => result.data);

    const getById = (id) =>
        $http
            .get(`${BASE}/id/${id}`)
            .then(result => result.data);

    const findByRelationshipKindId = (id) =>
        $http
            .get(`${BASE}/relationship-kind/${id}`)
            .then(result => result.data);

    const findByEntityReference = (ref) =>
        $http
            .get(`${BASE}/kind/${ref.kind}/id/${ref.id}`)
            .then(result => result.data);

    const getViewByRelationshipKindId = (id, opts) =>
        $http
            .post(`${BASE}/relationship-kind/${id}/view`, opts)
            .then(result => result.data);

    return {
        getById,
        findByLegalEntityId,
        findByEntityReference,
        findByRelationshipKindId,
        getViewByRelationshipKindId
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


export const serviceName = "LegalEntityRelationshipStore";


export const LegalEntityRelationshipStore_API = {
    getById: {
        serviceName,
        serviceFnName: "getById",
        description: "executes getById"
    },
    findByLegalEntityId: {
        serviceName,
        serviceFnName: "findByLegalEntityId",
        description: "executes findByLegalEntityId"
    },
    findByRelationshipKindId: {
        serviceName,
        serviceFnName: "findByRelationshipKindId",
        description: "executes findByRelationshipKindId"
    },
    findByEntityReference: {
        serviceName,
        serviceFnName: "findByEntityReference",
        description: "executes findByEntityReference"
    },
    getViewByRelationshipKindId: {
        serviceName,
        serviceFnName: "getViewByRelationshipKindId",
        description: "executes getViewByRelationshipKindId"
    },
};

export default {
    serviceName,
    store
};
