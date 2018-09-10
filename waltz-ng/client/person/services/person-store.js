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

function store($http,
                 BaseApiUrl) {
    const BASE = `${BaseApiUrl}/person`;

    const unknownPerson = (id, displayName = "Unknown") => {
        return {
            id: id,
            displayName: displayName
        }
    };

    const getByEmployeeId = (empId) => $http
        .get(`${BASE}/employee-id/${empId}`)
        .then(result => result.data);


    const findByUserId = (userId) => $http
        .get(`${BASE}/user-id/${userId}`)
        .then(result => result.data);

    const getById = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(result => (result.data)? result.data : unknownPerson(id));

    const findDirects = (empId) => $http
        .get(`${BASE}/employee-id/${empId}/directs`)
        .then(result => result.data);


    const findManagers = (empId) => $http
        .get(`${BASE}/employee-id/${empId}/managers`)
        .then(result => result.data);


    const countCumulativeReportsByKind = (empId) => $http
        .get(`${BASE}/employee-id/${empId}/count-cumulative-reports`)
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
        countCumulativeReportsByKind,
        search
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


const serviceName = "PersonStore";


export const PersonStore_API = {
    getByEmployeeId: {
        serviceName,
        serviceFnName: "getByEmployeeId",
        description: "get person by employee id"
    },
    getById: {
        serviceName,
        serviceFnName: "getById",
        description: "get person by id"
    },
    findByUserId: {
        serviceName,
        serviceFnName: "findByUserId",
        description: "find person by user id"
    },
    findDirects: {
        serviceName,
        serviceFnName: "findDirects",
        description: "find direct reports for person"
    },
    findManagers: {
        serviceName,
        serviceFnName: "findManagers",
        description: "find managers for person"
    },
    countCumulativeReportsByKind: {
        serviceName,
        serviceFnName: "countCumulativeReportsByKind",
        description: "count Cumulative Reports [empId]"
    },
    search: {
        serviceName,
        serviceFnName: "search",
        description: "search for people"
    }
};


export default {
    store,
    serviceName
};
