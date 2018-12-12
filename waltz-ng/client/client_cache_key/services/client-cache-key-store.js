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

function store($http, BaseApiUrl) {
    const BASE = `${BaseApiUrl}/client-cache-key`;

    const findAll = () =>
        $http.get(`${BASE}/all`)
            .then(result => result.data);

    const getByKey = (key) => $http
        .get(`${BASE}/key/${key}`)
        .then(r => r.data);

    const createOrUpdate = (key) => $http
        .post(`${BASE}/update/${key}`)
        .then(r => r.data);

    return {
        findAll,
        getByKey,
        createOrUpdate
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


const serviceName = 'ClientCacheKeyStore';


export const ClientCacheKeyStore_API = {
    findAll: {
        serviceName,
        serviceFnName: 'findAll',
        description: 'finds all'
    },
    getByKey: {
        serviceName,
        serviceFnName: 'getByKey',
        description: 'getByKey'
    },
    createOrUpdate: {
        serviceName,
        serviceFnName: 'createOrUpdate',
        description: 'createOrUpdate'
    }
};


export default {
    store,
    serviceName
};
