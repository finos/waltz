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


import { checkIsEntityRef } from "../../common/checks";

export function store($http, BaseApiUrl) {
    const BASE = `${BaseApiUrl}/change-set`;

    const getById = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(result => result.data);

    const findByParentRef = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${BASE}/parent/${ref.kind}/${ref.id}`)
            .then(result => result.data);
    };

    const findByPerson = (employeeId) => {
        return $http
            .get(`${BASE}/person/${employeeId}`)
            .then(result => result.data);
    };

    const findBySelector = (options) => $http
        .post(`${BASE}/selector`, options)
        .then(result => result.data);

    return {
        getById,
        findByParentRef,
        findByPerson,
        findBySelector
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl",
];


export const serviceName = "ChangeSetStore";


export const ChangeSetStore_API = {
    getById: {
        serviceName,
        serviceFnName: "getById",
        description: "retrieve a single change set (or null) given an id"
    },
    findByParentRef: {
        serviceName,
        serviceFnName: "findByParentRef",
        description: "find change sets for an associated parent ref"
    },
    findByPerson: {
        serviceName,
        serviceFnName: "findByPerson",
        description: "find change sets for a person by involvement"
    },
    findBySelector: {
        serviceName,
        serviceFnName: "findBySelector",
        description: "find change sets by selector"
    },
};

