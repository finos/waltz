/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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


function service($http, BaseApiUrl) {
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


    const update = (id, action) => $http
        .post(`${BASE}/${id}`, action)
        .then(x => x.data);


    const findRelatedById = (id) => $http
        .get(`${BASE}/id/${id}/related`)
        .then(x => x.data);


    const findAllTags = () => $http
        .get(`${BASE}/tags`)
        .then(x => x.data);


    const findByTag = (tag) => $http
        .post(`${BASE}/tags`, tag)
        .then(x => x.data);


    const findBySelector = (options) => $http
        .post(`${BASE}/selector`, options)
        .then(x => x.data);


    const findByAssetCode = (assetCode) => $http
        .get(`${BASE}/asset-code/${assetCode}`)
            .then(result => result.data);


    return {
        getById,
        findAllTags,
        findByTag,
        getAppTagsById,
        findRelatedById,
        findByIds,
        findBySelector,
        findByAssetCode,
        countByOrganisationalUnit,
        registerNewApp,
        search,
        update
    };
}


service.$inject = [
    '$http',
    'BaseApiUrl',
];


export default service;
