
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

    const BASE = `${BaseApiUrl}/entity-named-note`;

    const findByEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http.get(`${BASE}/entity-ref/${ref.kind}/${ref.id}`)
            .then(result => result.data);
    };

    const save = (entityRef, noteTypeId, noteText) => {
        checkIsEntityRef(entityRef);
        return $http.put(`${BASE}/entity-ref/${entityRef.kind}/${entityRef.id}/${noteTypeId}`, noteText)
            .then(result => result.data);
    };

    const remove = (entityRef, noteTypeId) => {
        checkIsEntityRef(entityRef);
        return $http.delete(`${BASE}/entity-ref/${entityRef.kind}/${entityRef.id}/${noteTypeId}`)
            .then(result => result.data);
    };

    return {
        findByEntityReference,
        save,
        remove,
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = 'EntityNamedNoteStore';


export const EntityNamedNoteStore_API = {
    findByEntityReference: {
        serviceName,
        serviceFnName: 'findByEntityReference',
        description: 'finds entity named notes by entity reference'
    },
    save: {
        serviceName,
        serviceFnName: 'save',
        description: 'saves an entity named note'
    },
    remove: {
        serviceName,
        serviceFnName: 'remove',
        description: 'removes an entity named note'
    }
};