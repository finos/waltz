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
    const BASE = `${BaseApiUrl}/app`;


    const getById = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(result => result.data);


    const countByOrganisationalUnit = () => $http
        .get(`${BASE}/count-by/org-unit`)
        .then(result => result.data);


    const registerNewApp = (registrationRequest) => $http
        .post(BASE, registrationRequest)
        .then(x => x.data);


    const search = (query) => $http
        .get(`${BASE}/search/${query}`)
        .then(x => x.data);


    const findByIds = (ids) => $http
        .post(`${BASE}/by-ids`, ids)
        .then(x => x.data);

    const findAll = (ids) => $http
        .get(`${BASE}/all`)
        .then(x => x.data);

    const update = (id, action) => $http
        .post(`${BASE}/${id}`, action)
        .then(x => x.data);


    const findRelatedById = (id) => $http
        .get(`${BASE}/id/${id}/related`)
        .then(x => x.data);


    const findBySelector = (options) => $http
        .post(`${BASE}/selector`, options)
        .then(x => x.data);


    const findByAssetCode = (assetCode) => $http
        .get(`${BASE}/asset-code/${assetCode}`)
            .then(result => result.data);

    const findByExternalId = (extId) => $http
        .get(`${BASE}/external-id/${extId}`)
            .then(result => result.data);


    return {
        getById,
        findRelatedById,
        findByIds,
        findAll,
        findBySelector,
        findByAssetCode,
        findByExternalId,
        countByOrganisationalUnit,
        registerNewApp,
        search,
        update
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl",
];


export const serviceName = "ApplicationStore";


export const ApplicationStore_API = {
    getById: {
        serviceName,
        serviceFnName: "getById",
        description: "retrieve a single app (or null) given an id"
    },
    findRelatedById: {
        serviceName,
        serviceFnName: "findRelatedById",
        description: "find related apps for the given app id"
    },
    findByIds: {
        serviceName,
        serviceFnName: "findByIds",
        description: "find apps for the given list of app ids"
    },
    findAll: {
        serviceName,
        serviceFnName: "findAll",
        description: "find all apps"
    },
    findBySelector: {
        serviceName,
        serviceFnName: "findBySelector",
        description: "find apps for the given selector options"
    },
    findByAssetCode: {
        serviceName,
        serviceFnName: "findByAssetCode",
        description: "executes findByAssetCode"
    },
    countByOrganisationalUnit: {
        serviceName,
        serviceFnName: "countByOrganisationalUnit",
        description: "returns number of apps in the given ou"
    },
    registerNewApp: {
        serviceName,
        serviceFnName: "registerNewApp",
        description: "registers a new application"
    },
    search: {
        serviceName,
        serviceFnName: "search",
        description: "find apps for the given search terms"
    },
    update: {
        serviceName,
        serviceFnName: "update",
        description: "updates an application"
    }
};

