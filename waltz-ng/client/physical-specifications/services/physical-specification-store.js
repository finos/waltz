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

import {checkIsIdSelector, checkIsEntityRef} from "../../common/checks";

export function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/physical-specification`;


    const findByEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${base}/application/${ref.kind}/${ref.id}`)
            .then(r => r.data);
    };


    const findBySelector = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${base}/selector`, options)
            .then(r => r.data);
    };


    const getById = (id) => $http
        .get(`${base}/id/${id}`)
        .then(r => r.data);


    const search = (terms) => $http
        .get(`${base}/search/${terms}`)
        .then(r => r.data);


    const deleteById = (id) => $http
            .delete(`${base}/${id}`)
            .then(r => r.data);


    return {
        findByEntityReference,
        findBySelector,
        getById,
        deleteById,
        search
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = 'PhysicalSpecificationStore';


export const PhysicalSpecificationStore_API = {
    findByEntityReference: {
        serviceName,
        serviceFnName: 'findByEntityReference',
        description: 'executes findByEntityReference'
    },
    findBySelector: {
        serviceName,
        serviceFnName: 'findBySelector',
        description: 'executes findBySelector'
    },
    getById: {
        serviceName,
        serviceFnName: 'getById',
        description: 'executes getById'
    },
    deleteById: {
        serviceName,
        serviceFnName: 'deleteById',
        description: 'executes deleteById'
    },
    search: {
        serviceName,
        serviceFnName: 'search',
        description: 'executes search'
    },
};