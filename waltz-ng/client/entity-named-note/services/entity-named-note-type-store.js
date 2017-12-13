
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
