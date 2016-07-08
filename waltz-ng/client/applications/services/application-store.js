/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */


function service($http, BaseApiUrl) {
    const BASE = `${BaseApiUrl}/app`;


    
    const getById = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(result => result.data);


    const findByOrgUnit = (ouId) => $http
        .get(`${BASE}/org-unit/${ouId}`)
        .then(result => result.data);


    const findByOrgUnitTree = (ouId) => $http
        .get(`${BASE}/org-unit-tree/${ouId}`)
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


    return {
        getById,
        findAllTags,
        findByTag,
        findRelatedById,
        findByOrgUnit,
        findByOrgUnitTree,
        findByIds,
        findBySelector,
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
