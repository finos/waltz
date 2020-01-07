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
