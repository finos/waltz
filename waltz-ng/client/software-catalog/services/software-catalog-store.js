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
import _ from "lodash";


export function store($http, BaseApiUrl) {
    const BASE = `${BaseApiUrl}/software-catalog`;

    const findByAppIds = (ids = []) => {
        return $http.post(`${BASE}/apps`, ids)
            .then(r => r.data);
    };


    const findStatsForSelector = (id, kind, scope = "CHILDREN") => {
        const options = _.isObject(id)
            ? id
            : {scope, entityReference: {id, kind}};

        return $http.post(`${BASE}/stats`, options)
            .then(result => result.data);
    };


    const findBySelector = (options) => $http
        .post(`${BASE}/selector`, options)
        .then(x => x.data);


    const getByPackageId = (id) => $http
        .get(`${BASE}/package-id/${id}`)
        .then(r => r.data);


    const getByVersionId = (id) => $http
        .get(`${BASE}/version-id/${id}`)
        .then(r => r.data);


    const getByLicenceId = (id) => $http
        .get(`${BASE}/licence-id/${id}`)
        .then(r => r.data);


    return {
        findByAppIds,
        findBySelector,
        findStatsForSelector,
        getByPackageId,
        getByLicenceId,
        getByVersionId
    };
}

store.$inject = [
    "$http",
    "BaseApiUrl"
];


export const serviceName = "SoftwareCatalogStore";


export const SoftwareCatalogStore_API = {
    findByAppIds: {
        serviceName,
        serviceFnName: "findByAppIds",
        description: "retrieve catalog for a list of app ids"
    },
    findBySelector: {
        serviceName,
        serviceFnName: "findBySelector",
        description: "find software catalog for a given selector options"
    },
    findStatsForSelector: {
        serviceName,
        serviceFnName: "findStatsForSelector",
        description: "find software catalog stats the given selector options"
    },
    getByPackageId: {
        serviceName,
        serviceFnName: 'getByPackageId',
        description: 'executes getByPackageId'
    },
    getByVersionId: {
        serviceName,
        serviceFnName: 'getByVersionId',
        description: 'executes getByVersionId'
    },
};

