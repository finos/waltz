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

export function store($http, BaseApiUrl) {
    const BASE = `${BaseApiUrl}/change-unit`;

    const getById = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(result => result.data);

    const findBySubjectRef = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${BASE}/subject/${ref.kind}/${ref.id}`)
            .then(result => result.data);
    };

    const findByChangeSetId = (id) => $http
        .get(`${BASE}/change-set/${id}`)
        .then(result => result.data);

    const findBySelector = (options) => $http
        .post(`${BASE}/selector`, options)
        .then(result => result.data);

    const updateExecutionStatus = (cmd) => {
        return $http
            .post(`${BASE}/update/execution-status`, cmd)
            .then(result => result.data);
    };

    return {
        getById,
        findBySubjectRef,
        findBySelector,
        findByChangeSetId,
        updateExecutionStatus
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


export const serviceName = "ChangeUnitStore";


export const ChangeUnitStore_API = {
    getById: {
        serviceName,
        serviceFnName: "getById",
        description: "retrieve a single change set (or null) given an id"
    },
    findBySubjectRef: {
        serviceName,
        serviceFnName: "findBySubjectRef",
        description: "find change units for an associated subject ref"
    },
    findByChangeSetId: {
        serviceName,
        serviceFnName: "findByChangeSetId",
        description: "find change units for an associated change set"
    },
    findBySelector: {
        serviceName,
        serviceFnName: "findBySelector",
        description: "find change units by selector"
    },
    updateExecutionStatus: {
        serviceName,
        serviceFnName: "updateExecutionStatus",
        description: "updates the execution status of a change unit"
    }

};

