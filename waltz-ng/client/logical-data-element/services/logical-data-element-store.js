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

import {checkIsIdSelector} from "../../common/checks";


export function store($http,
                      baseUrl) {

    const BASE = `${baseUrl}/logical-data-element`;

    const getById = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(result => result.data);

    const getByExternalId = (id) => $http
        .get(`${BASE}/external-id/${id}`)
        .then(result => result.data);

    const findAll = (ids) => $http
        .get(`${BASE}/all`)
        .then(x => x.data);

    const findBySelector = (selector) => {
        checkIsIdSelector(selector);
        return $http
            .post(`${BASE}/selector`, selector)
            .then(result => result.data);
    };

    return {
        getById,
        getByExternalId,
        findAll,
        findBySelector
    };
}

store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = "LogicalDataElementStore";


export const LogicalDataElementStore_API = {
    getById: {
        serviceName,
        serviceFnName: 'getById',
        description: 'executes getById'
    },
    getByExternalId: {
        serviceName,
        serviceFnName: 'getByExternalId',
        description: 'executes getByExternalId'
    },
    findAll: {
        serviceName,
        serviceFnName: 'findAll',
        description: 'executes findAll'
    },
    findBySelector: {
        serviceName,
        serviceFnName: 'findBySelector',
        description: 'executes findBySelector'
    }
};
