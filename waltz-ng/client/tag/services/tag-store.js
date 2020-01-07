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
import {checkIsEntityRef, checkIsStringList} from "../../common/checks";


export function store($http, base) {
    const BASE = `${base}/tag`;

    const getTagById = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(x => x.data);

    const findTagsByEntityRef = (ref) => $http
        .get(`${BASE}/entity/${ref.kind}/${ref.id}`)
        .then(x => x.data);

    const findTagsByEntityKind = (entityKind) => $http
        .get(`${BASE}/target-kind/${entityKind}`)
        .then(x => x.data);

    const update = (entityRef, tags = []) => {
        checkIsEntityRef(entityRef);
        checkIsStringList(tags);

        return $http
            .post(`${BASE}/entity/${entityRef.kind}/${entityRef.id}`, tags)
            .then(r => r.data);
    };

    return {
        getTagById,
        findTagsByEntityRef,
        findTagsByEntityKind,
        update
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = 'TagStore';


export const TagStore_API = {
    getTagById: {
        serviceName,
        serviceFnName: 'getTagById',
        description: 'executes getTagById'
    },
    findTagsByEntityRef: {
        serviceName,
        serviceFnName: 'findTagsByEntityRef',
        description: 'executes findTagsByEntityRef'
    },
    findTagsByEntityKind: {
        serviceName,
        serviceFnName: 'findTagsByEntityKind',
        description: 'executes findTagsByEntityKind'
    },
    update: {
        serviceName,
        serviceFnName: 'update',
        description: 'executes update'
    }
};

