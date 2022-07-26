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
    const BASE = `${BaseApiUrl}/assessment-definition`;


    const getById = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(result => result.data);

    const findAll = () => $http
        .get(BASE)
        .then(x => x.data);

    const findByKind = (kind) => $http
        .get(`${BASE}/kind/${kind}`)
        .then(x => x.data);

    const findByEntityReference = (ref) => $http
        .get(`${BASE}/kind/${ref.kind}/id/${ref.id}`)
        .then(x => x.data);


    return {
        getById,
        findAll,
        findByKind,
        findByEntityReference
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl',
];


export const serviceName = 'AssessmentDefinitionStore';


export const AssessmentDefinitionStore_API = {
    getById: {
        serviceName,
        serviceFnName: 'getById',
        description: 'retrieve a single definition (or null) given an id'
    },
    findAll: {
        serviceName,
        serviceFnName: 'findAll',
        description: 'find all assessment definitions'
    },
    findByKind: {
        serviceName,
        serviceFnName: 'findByKind',
        description: 'find all assessment definitions for an entity kind '
    },
    findByEntityReference: {
        serviceName,
        serviceFnName: 'findByEntityReference',
        description: 'find all assessment definitions for a parent entity ref'
    }
};

