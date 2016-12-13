/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

function service($http, BaseApiUrl) {

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
        findDescendants,
        findImmediateHierarchy,
        search
    };

}


service.$inject = [
    '$http',
    'BaseApiUrl'
];


export default service;

