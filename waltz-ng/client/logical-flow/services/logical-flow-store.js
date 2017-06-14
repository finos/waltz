/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
import {checkIsEntityRef, checkIsIdSelector} from '../../common/checks'

export function store($http, BaseApiUrl) {
    const BASE = `${BaseApiUrl}/logical-flow`;

    // --- FINDERS ---
    const getById = (id) => $http
        .get(`${BASE}/${id}`)
        .then(r => r.data);

    const findBySelector = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${BASE}/selector`, options)
            .then(r => r.data);
    };

    const findByEntityReference = (kind, id) => {
        const entityReference = _.isObject(kind)
            ? kind
            : { kind, id };

        checkIsEntityRef(entityReference);

        return $http
            .get(`${BASE}/entity/${entityReference.kind}/${entityReference.id}`)
            .then(result => result.data);
    };


    // --- STATS ---
    const calculateStats = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${BASE}/stats`, options)
            .then(r => r.data);
    };

    const countByDataType = () => $http
        .get(`${BASE}/count-by/data-type`)
        .then(result => result.data);

    // --- UPDATERS ---
    const removeFlow = (id) => $http
        .delete(`${BASE}/${id}`)
        .then(r => r.data);

    const addFlow = (addFlowCmd) => $http
        .post(`${BASE}`, addFlowCmd)
        .then(r => r.data);

    const cleanupOrphans = () => $http
        .post(`${BASE}/cleanup-orphans`, null)
        .then(r => r.data);

    return {
        findBySelector,
        findByEntityReference,
        calculateStats,
        countByDataType,
        removeFlow,
        getById,
        addFlow,
        cleanupOrphans
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = 'LogicalFlowStore';


export const LogicalFlowStore_API = {
    findBySelector: {
        serviceName,
        serviceFnName: 'findBySelector',
        description: 'find logical flows for a selector'
    },
    findByEntityReference: {
        serviceName,
        serviceFnName: 'findByEntityReference',
        description: 'find logical flows involving a given entity'
    },
    calculateStats: {
        serviceName,
        serviceFnName: 'calculateStats',
        description: 'calculate statistics for flows'
    },
    countByDataType: {
        serviceName,
        serviceFnName: 'countByDataType',
        description: 'summarizes flows by their data types'
    },
    removeFlow: {
        serviceName,
        serviceFnName: 'removeFlow',
        description: 'removes a single logical flow'
    },
    getById: {
        serviceName,
        serviceFnName: 'getById',
        description: 'retrieve a single logical flow (or null) given an id'
    },
    addFlow: {
        serviceName,
        serviceFnName: 'addFlow',
        description: 'adds a single logical flow'
    },
    cleanupOrphans: {
        serviceName,
        serviceFnName: 'cleanupOrphans',
        description: 'mark flows as removed if either endpoint is missing'
    },
};


