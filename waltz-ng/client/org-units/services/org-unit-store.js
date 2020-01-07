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

    const BASE = `${BaseApiUrl}/org-unit`;


    const getById = (id) => $http
        .get(`${BASE}/${id}`)
        .then(result => result.data);


    const findAll = () => $http
        .get(BASE)
        .then(result => result.data);


    const findByIds = (ids) => $http
        .post(`${BASE}/by-ids`, ids)
        .then(result => result.data);


    const findRelatedByEntityRef = (ref) => $http
        .get(`${BASE}/related/${ref.kind}/${ref.id}`, ref)
        .then(result => result.data);


    const findDescendants = (id) => $http
        .get(`${BASE}/${id}/descendants`)
        .then(result => result.data);


    /**
     * id -> [{level, entityReference}...]
     * @param id
     */
    const findImmediateHierarchy = (id) => $http
        .get(`${BASE}/${id}/immediate-hierarchy`)
        .then(result => result.data);


    const search = (query) => $http
        .get(`${BASE}/search/${query}`)
        .then(x => x.data);


    return {
        getById,
        findAll,
        findByIds,
        findRelatedByEntityRef,
        findDescendants,
        findImmediateHierarchy,
        search
    };

}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = "OrgUnitStore";

export const OrgUnitStore_API = {
    getById: {
        serviceName,
        serviceFnName: 'getById',
        description: 'executes getById'
    },
    findAll: {
        serviceName,
        serviceFnName: 'findAll',
        description: 'executes findAll'
    },
    findByIds: {
        serviceName,
        serviceFnName: 'findByIds',
        description: 'executes findByIds'
    },
    findRelatedByEntityRef: {
        serviceName,
        serviceFnName: 'findRelatedByEntityRef',
        description: 'executes findRelatedByEntityRef'
    },
    findDescendants: {
        serviceName,
        serviceFnName: 'findDescendants',
        description: 'executes findDescendants'
    },
    findImmediateHierarchy: {
        serviceName,
        serviceFnName: 'findImmediateHierarchy',
        description: 'executes findImmediateHierarchy'
    },
    search: {
        serviceName,
        serviceFnName: 'search',
        description: 'executes search'
    },
}

