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


function store($http, baseApiUrl) {
    const baseUrl = `${baseApiUrl}/taxonomy-management`;

    const preview = (cmd) => $http
        .post(`${baseUrl}/preview`, cmd)
        .then(d => d.data);

    const submitPendingChange = (cmd) => $http
        .post(`${baseUrl}/pending-changes`, cmd)
        .then(d => d.data);

    const findAllChangesByDomain = (domainRef) => $http
        .get(`${baseUrl}/all/by-domain/${domainRef.kind}/${domainRef.id}`)
        .then(d => d.data);

    const findPendingChangesByDomain = (domainRef) => $http
        .get(`${baseUrl}/pending-changes/by-domain/${domainRef.kind}/${domainRef.id}`)
        .then(d => d.data);

    const previewById = (changeId) => $http
        .get(`${baseUrl}/pending-changes/id/${changeId}/preview`)
        .then(d => d.data);

    const removeById = (changeId) => $http
        .delete(`${baseUrl}/pending-changes/id/${changeId}`)
        .then(d => d.data);

    const applyPendingChange = (changeId) => $http
        .post(`${baseUrl}/pending-changes/id/${changeId}/apply`)
        .then(d => d.data);

    return {
        findPendingChangesByDomain,
        findAllChangesByDomain,
        preview,
        previewById,
        removeById,
        submitPendingChange,
        applyPendingChange
    };

}

store.$inject = ["$http", "BaseApiUrl"];


const serviceName = "TaxonomyManagementStore";


export default {
    serviceName,
    store
};


export const TaxonomyManagementStore_API = {
    applyPendingChange: {
        serviceName,
        serviceFnName: "applyPendingChange",
        description: "applyPendingChange [ changeId ]"
    },
    findAllChangesByDomain: {
        serviceName,
        serviceFnName: "findAllChangesByDomain",
        description: "findAllChangesByDomain [ domainRef ]"
    },
    findPendingChangesByDomain: {
        serviceName,
        serviceFnName: "findPendingChangesByDomain",
        description: "findPendingChangesByDomain [ domainRef ]"
    },
    preview: {
        serviceName,
        serviceFnName: "preview",
        description: "preview the effect of a change [ changeId ]"
    },
    previewById: {
        serviceName,
        serviceFnName: "previewById",
        description: "preview the effect of a pending change [ changeId ]"
    },
    removeById: {
        serviceName,
        serviceFnName: "removeById",
        description: "remove [ changeId ]"
    },
    submitPendingChange: {
        serviceName,
        serviceFnName: "submitPendingChange",
        description: "submit the command for later execution [ change ]"
    }
};