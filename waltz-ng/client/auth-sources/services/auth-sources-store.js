
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

import _ from 'lodash';
import {
    checkIsAuthSourceCreateCommand,
    checkIsAuthSourceUpdateCommand, checkIsEntityRef,
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


    const findNonAuthSources = (entityRef) => {
        checkIsEntityRef(entityRef);
        return $http
            .get(`${BASE}/non-auth-for/${entityRef.kind}/${entityRef.id}`)
            .then(r => r.data);
    };

    const findAuthSources = (entityRef) => {
        checkIsEntityRef(entityRef);
        return $http
            .get(`${BASE}/auth-for/${entityRef.kind}/${entityRef.id}`)
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