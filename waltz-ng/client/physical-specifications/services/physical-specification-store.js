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

import {checkIsEntityRef, checkIsIdSelector} from "../../common/checks";

export function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/physical-specification`;


    const findByEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${base}/application/${ref.kind}/${ref.id}`)
            .then(r => r.data);
    };


    const findByExternalId = (externalId) => {
        return $http
            .get(`${base}/external-id/${externalId}`)
            .then(r => r.data);
    };


    const findBySelector = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${base}/selector`, options)
            .then(r => r.data);
    };


    const findByIds = (ids = []) => {
        return $http
            .post(`${base}/ids`, ids)
            .then(r => r.data);
    };


    const getById = (specificationId) => $http
        .get(`${base}/id/${specificationId}`)
        .then(r => r.data);


    const findPermissionsForSpec = (specificationId) => $http
        .get(`${base}/id/${specificationId}/permissions`)
        .then(r => r.data);

    const search = (terms) => $http
        .post(`${base}/search`, terms)
        .then(r => r.data);


    const deleteById = (specificationId) => $http
        .delete(`${base}/${specificationId}`)
        .then(r => r.data);

    const updateAttribute = (flowId, command) => $http
        .post(`${base}/id/${flowId}/attribute`, command)
        .then(r => r.data);


    return {
        findByEntityReference,
        findBySelector,
        findByIds,
        findByExternalId,
        getById,
        deleteById,
        search,
        updateAttribute,
        findPermissionsForSpec
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


export const serviceName = "PhysicalSpecificationStore";


export const PhysicalSpecificationStore_API = {
    findByEntityReference: {
        serviceName,
        serviceFnName: "findByEntityReference",
        description: "executes findByEntityReference"
    },
    findBySelector: {
        serviceName,
        serviceFnName: "findBySelector",
        description: "executes findBySelector"
    },
    findByIds: {
        serviceName,
        serviceFnName: "findByIds",
        description: "executes findByIds"
    },
    findByExternalId: {
        serviceName,
        serviceFnName: "findByExternalId",
        description: "executes findByExternalId"
    },
    getById: {
        serviceName,
        serviceFnName: "getById",
        description: "executes getById"
    },
    deleteById: {
        serviceName,
        serviceFnName: "deleteById",
        description: "executes deleteById"
    },
    search: {
        serviceName,
        serviceFnName: "search",
        description: "executes search"
    },
    updateAttribute: {
        serviceName,
        serviceFnName: "updateAttribute",
        description: "executes updateAttribute"
    },
    findPermissionsForSpec: {
        serviceName,
        serviceFnName: "findPermissionsForSpec",
        description: "returns permissions for spec"
    }
};