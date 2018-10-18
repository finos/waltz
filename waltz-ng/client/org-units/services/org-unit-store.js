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

    const BASE = `${BaseApiUrl}/org-unit`;


    const getById = (id) => $http
        .get(`${BASE}/${id}`)
        .then(result => result.data);


    const findAll = () => $http
        .get(BASE)
        .then(result => result.data);


    const findByIds = (ids) => $http
        .post(`${BASE}/by-ids`, ids)
        .then(result => result.data);


    const findRelatedByEntityRef = (ref) => $http
        .get(`${BASE}/related/${ref.kind}/${ref.id}`, ref)
        .then(result => result.data);


    const findDescendants = (id) => $http
        .get(`${BASE}/${id}/descendants`)
        .then(result => result.data);


    /**
     * id -> [{level, entityReference}...]
     * @param id
     */
    const findImmediateHierarchy = (id) => $http
        .get(`${BASE}/${id}/immediate-hierarchy`)
        .then(result => result.data);


    const search = (query) => $http
        .get(`${BASE}/search/${query}`)
        .then(x => x.data);


    return {
        getById,
        findAll,
        findByIds,
        findRelatedByEntityRef,
        findDescendants,
        findImmediateHierarchy,
        search
    };

}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = "OrgUnitStore";

export const OrgUnitStore_API = {
    getById: {
        serviceName,
        serviceFnName: 'getById',
        description: 'executes getById'
    },
    findAll: {
        serviceName,
        serviceFnName: 'findAll',
        description: 'executes findAll'
    },
    findByIds: {
        serviceName,
        serviceFnName: 'findByIds',
        description: 'executes findByIds'
    },
    findRelatedByEntityRef: {
        serviceName,
        serviceFnName: 'findRelatedByEntityRef',
        description: 'executes findRelatedByEntityRef'
    },
    findDescendants: {
        serviceName,
        serviceFnName: 'findDescendants',
        description: 'executes findDescendants'
    },
    findImmediateHierarchy: {
        serviceName,
        serviceFnName: 'findImmediateHierarchy',
        description: 'executes findImmediateHierarchy'
    },
    search: {
        serviceName,
        serviceFnName: 'search',
        description: 'executes search'
    },
}

