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

function store($http,
                 BaseApiUrl) {
    const BASE = `${BaseApiUrl}/person`;


    const getByEmployeeId = (empId) => $http
        .get(`${BASE}/employee-id/${empId}`)
        .then(result => result.data);


    const findByUserId = (userId) => $http
        .get(`${BASE}/user-id/${userId}`)
        .then(result => result.data);

    const getById = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(result => result.data);

    const findDirects = (empId) => $http
        .get(`${BASE}/employee-id/${empId}/directs`)
        .then(result => result.data);


    const findManagers = (empId) => $http
        .get(`${BASE}/employee-id/${empId}/managers`)
        .then(result => result.data);


    const search = (query) => $http
        .get(`${BASE}/search/${query}`)
        .then(x => x.data);


    return {
        getByEmployeeId,
        getById,
        findByUserId,
        findDirects,
        findManagers,
        search
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;
