/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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


import { checkIsEntityRef } from "../../common/checks";

export function store($http, BaseApiUrl) {
    const BASE = `${BaseApiUrl}/change-unit`;

    const getById = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(result => result.data);

    const findBySubjectRef = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${BASE}/subject/${ref.kind}/${ref.id}`)
            .then(result => result.data);
    };

    const findByChangeSetId = (id) => $http
        .get(`${BASE}/change-set/${id}`)
        .then(result => result.data);


    const updateExecutionStatus = (cmd) => {
        return $http
            .post(`${BASE}/update/execution-status`, cmd)
            .then(result => result.data);
    };

    return {
        getById,
        findBySubjectRef,
        findByChangeSetId,
        updateExecutionStatus
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl",
];


export const serviceName = "ChangeUnitStore";


export const ChangeUnitStore_API = {
    getById: {
        serviceName,
        serviceFnName: "getById",
        description: "retrieve a single change set (or null) given an id"
    },
    findBySubjectRef: {
        serviceName,
        serviceFnName: "findBySubjectRef",
        description: "find change units for an associated subject ref"
    },
    findByChangeSetId: {
        serviceName,
        serviceFnName: "findByChangeSetId",
        description: "find change units for an associated change set"
    },
    updateExecutionStatus: {
        serviceName,
        serviceFnName: "updateExecutionStatus",
        description: "updates the execution status of a change unit"
    }
};

