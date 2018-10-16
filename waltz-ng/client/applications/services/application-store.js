/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


export function store($http, BaseApiUrl) {
    const BASE = `${BaseApiUrl}/app`;


    const getById = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(result => result.data);


    const getAppTagsById = (id) => $http
        .get(`${BASE}/id/${id}/tags`)
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


    return {
        getById,
        getAppTagsById,
        findRelatedById,
        findByIds,
        findAll,
        findBySelector,
        findByAssetCode,
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
    getAppTagsById: {
        serviceName,
        serviceFnName: "getAppTagsById",
        description: "find tags for the given app"
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

