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

export function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/entity-relationship`;


    const remove = (rel) => {
        const url = `${BASE}/relationship/${rel.a.kind}/${rel.a.id}/${rel.b.kind}/${rel.b.id}/${rel.relationship}`;
        return $http
            .delete(url);
    };


    const create = (rel) => {
        const url = `${BASE}/relationship/${rel.a.kind}/${rel.a.id}/${rel.b.kind}/${rel.b.id}/${rel.relationship}`;
        return $http
            .post(url, {});
    };


    /** this is a 'find' not a 'get' as 'undirected' may return two hits */
    const findBetweenEntities = (a,
                                 b,
                                 directed = true, // true == directed | false == undirected
                                 relationshipKinds = [] /*optional*/) => {
        const url = `${BASE}/relationship/${a.kind}/${a.id}/${b.kind}/${b.id}`;

        const params = {
            directed,
            relationshipKind: relationshipKinds
        };

        return $http
            .get(url, { params})
            .then(result => result.data);
    };


    const findForEntity = (ref,
                           directionality = "ANY",  // ANY | SOURCE | TARGET
                           relationshipKinds = []) => {

        const url = `${BASE}/entity/${ref.kind}/${ref.id}`;

        const params = {
            directionality,
            relationshipKind: relationshipKinds
        };

        return $http
            .get(url, { params})
            .then(result => result.data);
    };

    return {
        findBetweenEntities,
        findForEntity,
        remove,
        create
    };

}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


export const serviceName = "EntityRelationshipStore";


export const EntityRelationshipStore_API = {
    findBetweenEntities: {
        serviceName,
        serviceFnName: "findBetweenEntities",
        description: "executes findBetweenEntities [ref, directed? (T|f), List?<RelationshipKinds>]"
    },
    findForEntity: {
        serviceName,
        serviceFnName: "findForEntity",
        description: "executes findForEntity [ref, directionality? (_ANY_|SOURCE|TARGET), List?<RelationshipKinds>]"
    },
    remove: {
        serviceName,
        serviceFnName: "remove",
        description: "executes remove [rel]"
    },
    create: {
        serviceName,
        serviceFnName: "create",
        description: "executes create [rel]"
    },
};
