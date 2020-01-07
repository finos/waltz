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
