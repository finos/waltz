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

export function store($http, baseUrl) {

    const BASE = `${baseUrl}/database-usage`;

    const findByEntityReference = (ref) =>
        $http
            .get(`${BASE}/ref/${ref.kind}/${ref.id}`)
            .then(result => result.data);

    const findByDatabaseId = (id) =>
        $http
            .get(`${BASE}/database-id/${id}`)
            .then(result => result.data);

    return {
        findByEntityReference,
        findByDatabaseId
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


export const serviceName = "DatabaseUsageStore";


export const DatabaseUsageStore_API = {
    findByEntityReference: {
        serviceName,
        serviceFnName: "findByEntityReference",
        description: "executes findByEntityReference (entityRef)"
    },
    findByDatabaseId: {
        serviceName,
        serviceFnName: "findByDatabaseId",
        description: "executes findByDatabaseId (databaseId)"
    }
};



export default {
    serviceName,
    store
};