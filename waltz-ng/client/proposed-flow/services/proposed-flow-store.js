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

export function store($http) {
    const base = "api";
    const mcBase = `${base}/mc`;
    const personBase = `${base}/person`;
    const physicalSpecBase=`${base}/physical-specification`;


    const getById = (id) => {
        return $http
            .get(`${mcBase}/propose-flow/id/${id}`)
            .then(r => r.data);
    };

    const getByUserEmails = (emails) => {
        return $http
            .post(`${personBase}/user-emails`, emails)
            .then(r => r.data);
    };

    const editValidation = (id) => {
        return $http
            .get(`${physicalSpecBase}/id/${id}/validate-edit`)
            .then(r => r.data);
    }

    return {
        getById,
        getByUserEmails,
        editValidation
    };
}

store.$inject = [
    "$http"
];

export const serviceName = "ProposedFlowStore";

export const ProposedFlowStore_API = {
    getById: {
        serviceName,
        serviceFnName: "getById",
        description: "executes getById"
    },
    getByUserEmails: {
        serviceName,
        serviceFnName: "getByUserEmails",
        description: "finds people by a list of user emails"
    },
    editValidation: {
        serviceName,
        serviceFnName: "editValidation",
        description: "physical specification validation for edit"
    }
};