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


export function store($http, baseUrl) {

    const BASE = `${baseUrl}/server-info`;

    const findByAssetCode = (assetCode) =>
        $http
            .get(`${BASE}/asse-code/${assetCode}`)
            .then(result => result.data);

    const findByAppId = (appId) =>
        $http
            .get(`${BASE}/app-id/${appId}`)
            .then(result => result.data);


    const findStatsForSelector = (id, kind, scope = "EXACT") => {
        const options = _.isObject(id)
            ? id
            : {scope, entityReference: {id, kind}};
        return $http
            .post(`${BASE}/apps/stats`, options)
            .then(result => result.data);
    };


    const findBasicStatsForSelector = (selectionOptions) => {
        return $http
            .post(`${BASE}/apps/stats/basic`, selectionOptions)
            .then(result => result.data);
    };


    const getById = (serverId) =>
        $http
            .get(`${BASE}/${serverId}`)
            .then(result => result.data);


    const getByExternalId = (externalId) =>
        $http
            .get(`${BASE}/external-id/${externalId}`)
            .then(result => result.data);


    const getByHostname = (hostname) =>
        $http
            .get(`${BASE}/hostname/${hostname}`)
            .then(result => result.data);


    return {
        findByAssetCode,
        findByAppId,
        findStatsForSelector,
        findBasicStatsForSelector,
        getById,
        getByExternalId,
        getByHostname
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


export const serviceName = "ServerInfoStore";


export const ServerInfoStore_API = {
    findByAssetCode: {
        serviceName,
        serviceFnName: "findByAssetCode",
        description: "executes findByAssetCodes"
    },
    findByAppId: {
        serviceName,
        serviceFnName: "findByAppId",
        description: "executes findByAppId"
    },
    findStatsForSelector: {
        serviceName,
        serviceFnName: "findStatsForSelector",
        description: "executes findStatsForSelector - gives totals and breakdowns by location, opsys and hardware (etc) "
    },
    findBasicStatsForSelector: {
        serviceName,
        serviceFnName: "findBasicStatsForSelector",
        description: "executes findBasicStatsForSelector - gives just the total, virtual and physical counts"
    },
    getById: {
        serviceName,
        serviceFnName: "getById",
        description: "executes getById"
    },
    getByExternalId: {
        serviceName,
        serviceFnName: "getByExternalId",
        description: "executes getByExternalId"
    },
    getByHostname: {
        serviceName,
        serviceFnName: "getByHostname",
        description: "executes getByHostname"
    },
};



export default {
    serviceName,
    store
};