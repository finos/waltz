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


function store($http, baseApiUrl) {
    const baseUrl = `${baseApiUrl}/taxonomy-management`;

    const preview = (cmd) => $http
        .post(`${baseUrl}/preview`, cmd)
        .then(d => d.data);

    const previewByChangeId = (changeId) => $http
        .get(`${baseUrl}/pending-changes/${changeId}/preview`)
        .then(d => d.data);

    /*
    const previewById = (id) => $http
        .get(`${baseUrl}/pending-change/${id}`)
        .then(d => d.data);
    */

    const submitPendingChange = (cmd) => $http
        .post(`${baseUrl}/pending-changes`, cmd)
        .then(d => d.data);


    const findPendingChangesByDomain = (domainRef) => $http
        .get(`${baseUrl}/pending-changes/by-domain/${domainRef.kind}/${domainRef.id}`)
        .then(d => d.data);

    const applyPendingChange = (cmdId) => $http
        .post(`${baseUrl}/pending-changes/id/${cmdId}/apply`)
        .then(d => d.data);

    return {
        findPendingChangesByDomain,
        preview,
        previewByChangeId,
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
        description: "applyPendingChange [ cmdId ]"
    },
    findPendingChangesByDomain: {
        serviceName,
        serviceFnName: "findPendingChangesByDomain",
        description: "findPendingChangesByDomain [ domainRef ]"
    },
    preview: {
        serviceName,
        serviceFnName: "preview",
        description: "preview the effect of a command [ cmd ]"
    },
    previewByChangeId: {
        serviceName,
        serviceFnName: "previewByChangeId",
        description: "preview the effect of a pending command [ cmdId ]"
    },
    submitPendingChange: {
        serviceName,
        serviceFnName: "submitPendingChange",
        description: "submit the command for later execution [ cmd ]"
    }
};