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

import { checkIsIdSelector } from "../../common/checks";


export function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/licence`;

    const countApplications = () =>
        $http.get(`${BASE}/count/application`)
            .then(result => result.data);

    const findAll = () =>
        $http.get(`${BASE}/all`)
            .then(result => result.data);

    const findBySelector = (selector) => {
        checkIsIdSelector(selector);
        return $http
            .post(`${BASE}/selector`, selector)
            .then(r => r.data);
    };

    const getById = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(r => r.data);

    const getByExternalId = (id) => $http
        .get(`${BASE}/external-id/${id}`)
        .then(r => r.data);

    return {
        countApplications,
        findAll,
        findBySelector,
        getById,
        getByExternalId
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


const serviceName = "LicenceStore";


export default {
    serviceName,
    store
};


export const LicenceStore_API = {
    countApplications: {
        serviceName,
        serviceFnName: 'countApplications',
        description: 'executes countApplications'
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
    },
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
};
