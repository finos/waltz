
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
import {
    checkIsAuthSourceCreateCommand,
    checkIsAuthSourceUpdateCommand,
    checkIsIdSelector
} from "../../common/checks";


export function store($http, root) {

    const BASE = `${root}/authoritative-source`;


    const findByReference = (kind, id) => {
        const ref = _.isObject(kind) ? kind : { kind, id };
        return $http
            .get(`${BASE}/entity-ref/${ref.kind}/${ref.id}`)
            .then(result => result.data);
    };


    const findAll = (id) =>
        $http
            .get(BASE)
            .then(result => result.data);

    const findByApp = (id) =>
        $http
            .get(`${BASE}/app/${id}`)
            .then(result => result.data);


    const calculateConsumersForDataTypeIdSelector = (selector) => {
        checkIsIdSelector(selector);
        return $http
            .post(`${BASE}/data-type/consumers`, selector)
            .then(r => r.data);
    };


    const update = (cmd) => {
        checkIsAuthSourceUpdateCommand(cmd);
        return $http
            .put(BASE, cmd);
    };

    const remove = (id) =>
        $http
            .delete(`${BASE}/id/${id}`);

    const recalculateAll = () =>
        $http
            .get(`${BASE}/recalculate-flow-ratings`)
            .then(r => r.data);

    const insert = (command) => {
        checkIsAuthSourceCreateCommand(command);
        return $http
            .post(BASE, command);
    };

    const findNonAuthSources = (selector) => {
        checkIsIdSelector(selector);
        return $http
            .post(`${BASE}/non-auth`, selector)
            .then(r => r.data);
    };

    const findAuthSources = (selector) => {
        checkIsIdSelector(selector);
        return $http
            .post(`${BASE}/auth`, selector)
            .then(r => r.data);
    };

    const cleanupOrphans = () =>
        $http
            .get(`${BASE}/cleanup-orphans`)
            .then(r => r.data);

    return {
        calculateConsumersForDataTypeIdSelector,
        findByReference,
        findAll,
        findByApp,
        update,
        insert,
        recalculateAll,
        remove,
        findNonAuthSources,
        findAuthSources,
        cleanupOrphans
    };

}

store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = 'AuthSourcesStore';


export const AuthSourcesStore_API = {
    calculateConsumersForDataTypeIdSelector: {
        serviceName,
        serviceFnName: 'calculateConsumersForDataTypeIdSelector',
        description: 'calculateConsumersForDataTypeIdSelector'
    },
    findByReference: {
        serviceName,
        serviceFnName: 'findByReference',
        description: 'findByReference'
    },
    findAll: {
        serviceName,
        serviceFnName: 'findAll',
        description: 'findAll'
    },
    findByApp: {
        serviceName,
        serviceFnName: 'findByApp',
        description: 'findByApp'
    },
    update: {
        serviceName,
        serviceFnName: 'update',
        description: 'update'
    },
    insert: {
        serviceName,
        serviceFnName: 'insert',
        description: 'insert'
    },
    recalculateAll: {
        serviceName,
        serviceFnName: 'recalculateAll',
        description: 'recalculateAll'
    },
    remove: {
        serviceName,
        serviceFnName: 'remove',
        description: 'remove'
    },
    findNonAuthSources: {
        serviceName,
        serviceFnName: 'findNonAuthSources',
        description: 'findNonAuthSources'
    },
    findAuthSources: {
        serviceName,
        serviceFnName: 'findAuthSources',
        description: 'findAuthSources (entityRef)'
    },
    cleanupOrphans: {
        serviceName,
        serviceFnName: 'cleanupOrphans',
        description: 'cleanupOrphans'
    },
};