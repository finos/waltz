
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

    const BASE = `${BaseApiUrl}/entity-named-note-type`;


    const findAll = () =>
        $http.get(BASE)
            .then(result => result.data);

    const create = (cmd) =>
        $http.post(BASE, cmd)
            .then(result => result.data);

    const update = (id, cmd) =>
        $http.put(`${BASE}/${id}`, cmd)
            .then(result => result.data);

    const remove = (id) =>
        $http.delete(`${BASE}/${id}`)
            .then(result => result.data);

    return {
        findAll,
        create,
        update,
        remove
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = 'EntityNamedNoteTypeStore';


export const EntityNamedNoteTypeStore_API = {
    findAll: {
        serviceName,
        serviceFnName: 'findAll',
        description: 'finds all entity named note types'
    },
    create: {
        serviceName,
        serviceFnName: 'create',
        description: 'creates an entity named note type'
    },
    update: {
        serviceName,
        serviceFnName: 'update',
        description: 'updates an entity named note type'
    },
    remove: {
        serviceName,
        serviceFnName: 'remove',
        description: 'removes an entity named note type'
    }
};
