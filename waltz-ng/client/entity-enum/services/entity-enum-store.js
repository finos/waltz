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

import {checkIsEntityRef} from "../../common/checks";


export function store($http, BaseApiUrl) {
    const BASE = `${BaseApiUrl}/entity-enum`;

    // --- FINDERS ---
    const findDefinitionsByEntityKind = (entityKind) => $http
        .get(`${BASE}/definition/${entityKind}`)
        .then(r => r.data);

    const findValuesByEntity = (entityRef) => {
        checkIsEntityRef(entityRef);
        return $http
            .get(`${BASE}/value/${entityRef.kind}/${entityRef.id}`)
            .then(r => r.data);
    };

    return {
        findDefinitionsByEntityKind,
        findValuesByEntity
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = 'EntityEnumStore';


export const EntityEnumStore_API = {
    findDefinitionsByEntityKind: {
        serviceName,
        serviceFnName: 'findDefinitionsByEntityKind',
        description: 'find all entity enum definitions given an entity kind'
    },
    findValuesByEntity: {
        serviceName,
        serviceFnName: 'findValuesByEntity',
        description: 'find all entity enum values given an entity reference'
    }
};


