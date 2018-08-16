/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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
    const BASE = `${BaseApiUrl}/assessment-definition`;


    const getById = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(result => result.data);

    const findAll = () => $http
        .get(BASE)
        .then(x => x.data);

    const findByKind = (kind) => $http
        .get(`${BASE}/kind/${kind}`)
        .then(x => x.data);


    return {
        getById,
        findAll,
        findByKind
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl',
];


export const serviceName = 'AssessmentDefinitionStore';


export const AssessmentDefinitionStore_API = {
    getById: {
        serviceName,
        serviceFnName: 'getById',
        description: 'retrieve a single definition (or null) given an id'
    },
    findAll: {
        serviceName,
        serviceFnName: 'findAll',
        description: 'find all assessment definitions'
    },
    findByKind: {
        serviceName,
        serviceFnName: 'findByKind',
        description: 'find all assessment definitions for an entity kind '
    },
};

