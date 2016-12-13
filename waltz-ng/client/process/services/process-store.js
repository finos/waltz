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

    const BASE = `${BaseApiUrl}/process`;


    const getById = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(result => result.data);


    const findAll = () => $http
        .get(BASE)
        .then(result => result.data);


    const findSupportingCapabilities = (id) => $http
        .get(`${BASE}/id/${id}/capabilities`)
        .then(result => result.data);


    const findForApplication = (id) => $http
        .get(`${BASE}/application/${id}`)
        .then(result => result.data);


    const findForCapability = (id) => $http
        .get(`${BASE}/capability/${id}`)
        .then(result => result.data);


    return {
        getById,
        findAll,
        findForApplication,
        findForCapability,
        findSupportingCapabilities
    };

}


service.$inject = [
    '$http',
    'BaseApiUrl'
];


export default service;

