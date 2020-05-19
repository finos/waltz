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

    const findAllReportees = (empId) => $http
        .get(`${BASE}/employee-id/${empId}/reportees`)
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
        findAllReportees,
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
    findAllReportees: {
        serviceName,
        serviceFnName: "findAllReportees",
        description: "find all reportees for person"
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
