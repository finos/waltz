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

import {remote} from "./remote";

export function mkEntityRelationshipStore() {

    const getById = (id, force = false) => {
        return remote.fetchViewData(
            "GET",
            `api/entity-relationship/id/${id}`,
            null,
            {},
            {force});
    };

    const remove = (rel) => {
        return remote.execute(
            "DELETE",
            `api/entity-relationship/relationship/${rel.a.kind}/${rel.a.id}/${rel.b.kind}/${rel.b.id}/${rel.relationship}`,
            null);
    };


    const create = (rel) => {
        return remote.execute(
            "POST",
            `api/entity-relationship/relationship/${rel.a.kind}/${rel.a.id}/${rel.b.kind}/${rel.b.id}/${rel.relationship}`,
            null);
    };


    /** this is a 'find' not a 'get' as 'undirected' may return two hits */
    const findBetweenEntities = (a,
                                 b,
                                 directed = true, // true == directed | false == undirected
                                 relationshipKinds = [] /*optional*/) => {
        const params = {
            directed,
            relationshipKind: relationshipKinds
        };

        return remote.fetchViewList(
            "GET",
            `api/entity-relationship/relationship/${a.kind}/${a.id}/${b.kind}/${b.id}`,
            params);

    };


    const findForEntity = (ref,
                           directionality = "ANY",  // ANY | SOURCE | TARGET
                           relationshipKinds = [],
                           force = false) => {
        const params = {
            directionality,
            relationshipKind: relationshipKinds
        };

        return remote.fetchViewList(
            "GET",
            `api/entity-relationship/entity/${ref.kind}/${ref.id}`,
            params,
            { force });

    };

    return {
        getById,
        findBetweenEntities,
        findForEntity,
        remove,
        create
    };
}

export const entityRelationshipStore = mkEntityRelationshipStore();
