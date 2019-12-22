
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
import _ from 'lodash';


export function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/complexity`;

    const findByApplication = id => {
        console.log('DEPRECATED (1.2) - complexity-store:findByApplication');
        return $http
            .get(`${BASE}/application/${id}`)
            .then(result => result.data);
    };

    const findBySelector = (id, kind, scope = 'CHILDREN') => {
        const options = _.isObject(id)
            ? id
            : {scope, entityReference: {id, kind}};

        return $http
            .post(BASE, options)
            .then(result => result.data);
    };

    const recalculateAll = () =>
        $http
            .get(`${BASE}/rebuild`)
            .then(r => r.data);


    return {
        findByApplication,
        findBySelector,
        recalculateAll
    };
}

store.$inject = [
    '$http',
    'BaseApiUrl',
];

export const serviceName = 'ComplexityStore';

export const ComplexityStore_API = {
    findByApplication: {
        serviceName,
        serviceFnName: 'findByApplication',
        description: 'executes findByApplication (takes an id)'
    },
    findBySelector: {
        serviceName,
        serviceFnName: 'findBySelector',
        description: 'executes findBySelector'
    },
    recalculateAll: {
        serviceName,
        serviceFnName: 'recalculateAll',
        description: 'executes recalculateAll'
    },
};


