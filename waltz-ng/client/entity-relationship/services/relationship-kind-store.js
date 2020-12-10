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

    const BASE = `${baseUrl}/relationship-kind`;

    const findAll = () => {
        return $http
            .get(`${BASE}`)
            .then(result => result.data);
    };

    const findRelationshipKindsBetweenEntities = (parentRef, targetRef) => {
        return $http
            .get(`${BASE}/entities/${parentRef.kind}/${parentRef.id}/${targetRef.kind}/${targetRef.id}`)
            .then(result => result.data);
    };

    const create = (relationshipKind) => {
        return $http
            .post(`${BASE}/create`, relationshipKind)
            .then(result => result.data)
    };

    const update = (id, cmd) => {
        return $http
            .post(`${BASE}/id/${id}`, cmd)
            .then(result => result.data)
    };

    const remove = (id) => {
        return $http
            .delete(`${BASE}/id/${id}`)
            .then(result => result.data)
    };

    return {
        findAll,
        findRelationshipKindsBetweenEntities,
        create,
        update,
        remove
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


export const serviceName = "RelationshipKindStore";


export const RelationshipKindStore_API = {
    findAll: {
        serviceName,
        serviceFnName: "findAll",
        description: "executes findAll"
    },
    findRelationshipKindsBetweenEntities: {
        serviceName,
        serviceFnName: "findRelationshipKindsBetweenEntities",
        description: "finds allowed relationship kinds between entity a and entity b"
    },
    create: {
        serviceName,
        serviceFnName: "create",
        description: "creates relationship kind"
    },
    update: {
        serviceName,
        serviceFnName: "update",
        description: "updates relationship kind"
    },
    remove: {
        serviceName,
        serviceFnName: "remove",
        description: "removes relationship kind"
    }
};