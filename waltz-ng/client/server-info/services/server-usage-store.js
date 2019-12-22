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

    const BASE = `${baseUrl}/server-usage`;

    const findByReferencedEntity = (ref) =>
        $http
            .get(`${BASE}/ref/${ref.kind}/${ref.id}`)
            .then(result => result.data);

    const findByServerId = (id) =>
        $http
            .get(`${BASE}/server-id/${id}`)
            .then(result => result.data);

    return {
        findByReferencedEntity,
        findByServerId
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


export const serviceName = "ServerUsageStore";


export const ServerUsageStore_API = {
    findByReferencedEntity: {
        serviceName,
        serviceFnName: "findByReferencedEntity",
        description: "executes findByReferencedEntity (entityRef)"
    },
    findByServerId: {
        serviceName,
        serviceFnName: "findByServerId",
        description: "executes findByServerId (serverId)"
    }
};



export default {
    serviceName,
    store
};