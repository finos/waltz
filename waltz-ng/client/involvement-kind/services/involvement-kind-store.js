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

import { checkIsCreateInvolvementKindCommand } from "../../common/checks";


export function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/involvement-kind`;


    const findAll = () =>
        $http.get(BASE)
            .then(result => result.data);

    const findKeyInvolvementKindsByEntityKind = (entityKind) =>
        $http.get(`${BASE}/key-involvement-kinds/${entityKind}`)
            .then(result => result.data);


    const getById = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(r => r.data);


    /**
     * Creates a new Involvement Kind
     *
     * @param cmd : { name: <str>, description: <str> }
     * @returns {Promise.<TResult>|*}
     */
    const create = (cmd) => {
        checkIsCreateInvolvementKindCommand(cmd);
        return $http
            .post(`${BASE}/update`, cmd)
            .then(r => r.data);
    };


    const update = (cmd) => {
        return $http
            .put(`${BASE}/update`, cmd)
            .then(r => r.data);
    };

    const findUsageStats = () => {
        return $http
            .get(`${BASE}/usage-stats`)
            .then(r => r.data);
    };


    const deleteById = (id) => {
        return $http
            .delete(`${BASE}/${id}`)
            .then(r => r.data);
    };


    return {
        findAll,
        findKeyInvolvementKindsByEntityKind,
        getById,
        create,
        update,
        deleteById,
        findUsageStats
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


export const serviceName = "InvolvementKindStore";


export const InvolvementKindStore_API = {
    findAll: {
        serviceName,
        serviceFnName: "findAll",
        description: "executes findAll"
    },
    findKeyInvolvementKindsByEntityKind: {
        serviceName,
        serviceFnName: "findKeyInvolvementKindsByEntityKind",
        description: "executes findKeyInvolvementKindsByEntityKind"
    },
    getById: {
        serviceName,
        serviceFnName: "getById",
        description: "executes getById"
    },
    create: {
        serviceName,
        serviceFnName: "create",
        description: "executes create"
    },
    update: {
        serviceName,
        serviceFnName: "update",
        description: "executes update"
    },
    deleteById: {
        serviceName,
        serviceFnName: "deleteById",
        description: "executes deleteById"
    },
    findUsageStats: {
        serviceName,
        serviceFnName: "findUsageStats",
        description: "executes findUsageStats"
    }
};