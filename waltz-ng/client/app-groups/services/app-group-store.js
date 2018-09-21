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

    const BASE = `${BaseApiUrl}/app-group`;


    const getById = (id) => $http
        .get(`${BASE}/id/${id}/detail`)
        .then(result => result.data);


    const findByIds = (ids = []) => $http
        .post(`${BASE}/id`, ids)
        .then(result => result.data);


    const findMyGroupSubscriptions = (id) => $http
        .get(`${BASE}/my-group-subscriptions`)
        .then(result => result.data);


    const findPublicGroups = (id) => $http
        .get(`${BASE}/public`)
        .then(result => result.data);


    const findPrivateGroups = () => $http
        .get(`${BASE}/private`)
        .then(result => result.data);

    const findRelatedByEntityRef = (ref) => $http
        .get(`${BASE}/related/${ref.kind}/${ref.id}`)
        .then(result => result.data);


    const subscribe = (id) => $http
        .post(`${BASE}/id/${id}/subscribe`, {})
        .then(result => result.data);


    const unsubscribe = (id) => $http
        .post(`${BASE}/id/${id}/unsubscribe`, {})
        .then(result => result.data);


    const addOwner = (id, ownerId) => $http
        .post(`${BASE}/id/${id}/members/owners`, ownerId)
        .then(result => result.data);


    const removeOwner = (id, ownerId) => $http
        .delete(`${BASE}/id/${id}/members/owners/${ownerId}`)
        .then(result => result.data);


    const deleteGroup = (id) => $http
        .delete(`${BASE}/id/${id}`)
        .then(result => result.data);


    const addApplication = (groupId, applicationId) => $http
        .post(`${BASE}/id/${groupId}/applications`, applicationId)
        .then(result => result.data);


    const removeApplication = (groupId, applicationId) => $http
        .delete(`${BASE}/id/${groupId}/applications/${applicationId}`)
        .then(result => result.data);


    const addApplications = (groupId, applicationIds) => $http
        .post(`${BASE}/id/${groupId}/applications/list`, applicationIds)
        .then(result => result.data);


    const removeApplications = (groupId, applicationIds) => $http
            .post(`${BASE}/id/${groupId}/applications/list/remove`, applicationIds)
            .then(result => result.data);


    const addChangeInitiative = (groupId, changeInitiativeId) => $http
        .post(`${BASE}/id/${groupId}/change-initiatives`, changeInitiativeId)
        .then(result => result.data);


    const removeChangeInitiative = (groupId, changeInitiativeId) => $http
        .delete(`${BASE}/id/${groupId}/change-initiatives/${changeInitiativeId}`)
        .then(result => result.data);


    const updateGroupOverview = (groupId, data) => $http
        .post(`${BASE}/id/${groupId}`, data)
        .then(result => result.data);


    const createNewGroup = () => $http
        .post(BASE, {})
        .then(result => result.data);


    const search = (query) => $http
        .get(`${BASE}/search/${query}`)
        .then(x => x.data);


    return {
        findMyGroupSubscriptions,

        getById,
        findByIds,
        findPublicGroups,
        findPrivateGroups,
        findRelatedByEntityRef,

        subscribe,
        unsubscribe,
        addOwner,
        removeOwner,

        createNewGroup,
        deleteGroup,
        updateGroupOverview,

        addApplication,
        removeApplication,

        addApplications,
        removeApplications,

        addChangeInitiative,
        removeChangeInitiative,

        search
    };

}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = "AppGroupStore";


export const AppGroupStore_API = {
    findMyGroupSubscriptions: {
        serviceName,
        serviceFnName: 'findMyGroupSubscriptions',
        description: 'executes findMyGroupSubscriptions'
    },
    findRelatedByEntityRef: {
        serviceName,
        serviceFnName: 'findRelatedByEntityRef',
        description: 'executes findRelatedByEntityRef'
    },
    getById: {
        serviceName,
        serviceFnName: 'getById',
        description: 'executes getById'
    },
    findByIds: {
        serviceName,
        serviceFnName: 'findByIds',
        description: 'executes findByIds'
    },
    findPublicGroups: {
        serviceName,
        serviceFnName: 'findPublicGroups',
        description: 'executes findPublicGroups'
    },
    findPrivateGroups: {
        serviceName,
        serviceFnName: 'findPrivateGroups',
        description: 'executes findPrivateGroups'
    },
    subscribe: {
        serviceName,
        serviceFnName: 'subscribe',
        description: 'executes subscribe (params: id)'
    },
    unsubscribe: {
        serviceName,
        serviceFnName: 'unsubscribe',
        description: 'executes unsubscribe (params: id)'
    },
    addOwner: {
        serviceName,
        serviceFnName: 'addOwner',
        description: 'executes addOwner'
    },
    removeOwner: {
        serviceName,
        serviceFnName: 'removeOwner',
        description: 'executes removeOwner'
    },
    createNewGroup: {
        serviceName,
        serviceFnName: 'createNewGroup',
        description: 'executes createNewGroup'
    },
    deleteGroup: {
        serviceName,
        serviceFnName: 'deleteGroup',
        description: 'executes deleteGroup'
    },
    updateGroupOverview: {
        serviceName,
        serviceFnName: 'updateGroupOverview',
        description: 'executes updateGroupOverview'
    },
    addApplication: {
        serviceName,
        serviceFnName: 'addApplication',
        description: 'executes addApplication'
    },
    removeApplication: {
        serviceName,
        serviceFnName: 'removeApplication',
        description: 'executes removeApplication'
    },
    addApplications: {
        serviceName,
        serviceFnName: 'addApplications',
        description: 'executes addApplications'
    },
    removeApplications: {
        serviceName,
        serviceFnName: 'removeApplications',
        description: 'executes removeApplications'
    },
    addChangeInitiative: {
        serviceName,
        serviceFnName: 'addChangeInitiative',
        description: 'executes addChangeInitiative'
    },
    removeChangeInitiative: {
        serviceName,
        serviceFnName: 'removeChangeInitiative',
        description: 'executes removeChangeInitiative'
    },
    search: {
        serviceName,
        serviceFnName: 'search',
        description: 'find app groups for the given search terms'
    }
};
