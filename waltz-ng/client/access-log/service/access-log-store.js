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

import _ from "lodash";


export function store($http, BaseApiUrl) {
    const BASE = `${BaseApiUrl}/access-log`;

    const write = (state, params) => {
        const paramStr = _.isObject(params)
            ? JSON.stringify(params)
            : params;

        return $http
            .post(`${BASE}/${encodeURI(state)}`, paramStr)
            .then(r => r.data);
    };


    const findActiveUsers = (minutes) => $http
        .get(`${BASE}/active/${minutes}`)
        .then(r => r.data);


    const findForUserName = (userName, limit = null) => $http
        .get(`${BASE}/user/${userName}`, {params: {limit}})
        .then(r => r.data);


    return {
        findActiveUsers,
        findForUserName,
        write
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


export const serviceName = "AccessLogStore";


export const AccessLogStore_API = {
    write: {
        serviceName,
        serviceFnName: "write",
        description: "writes to access log table"
    },
    findActiveUsers: {
        serviceName,
        serviceFnName: "findActiveUsers",
        description: "executes findActiveUsers"
    },
    findForUserName: {
        serviceName,
        serviceFnName: "findForUserName",
        description: "executes findForUserName"
    }
}