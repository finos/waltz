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


    const findBySourceAndTargetEntityReferences = (sourceTargetReferences) => {
        return $http
            .post(`${BASE}/source-targets`, sourceTargetReferences)
            .then(result => result.data);
    };


    const findUpstreamFlowsForEntityReferences = (refs = []) => {
        return $http
            .post(`${BASE}/find-upstream-flows`, refs)
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

    const addFlows = (addFlowCmds) => $http
        .post(`${BASE}/list`, addFlowCmds)
        .then(r => r.data);

    const cleanupOrphans = () => $http
        .get(`${BASE}/cleanup-orphans`)
        .then(r => r.data);

    const cleanupSelfReferences = () => $http
        .get(`${BASE}/cleanup-self-references`)
        .then(r => r.data);

    return {
        findBySelector,
        findByEntityReference,
        findBySourceAndTargetEntityReferences,
        findUpstreamFlowsForEntityReferences,
        calculateStats,
        countByDataType,
        removeFlow,
        getById,
        addFlow,
        addFlows,
        cleanupOrphans,
        cleanupSelfReferences
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];

const serviceName = 'LogicalFlowStore';

export default {
    serviceName,
    store
};

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
    findBySourceAndTargetEntityReferences: {
        serviceName,
        serviceFnName: 'findBySourceAndTargetEntityReferences',
        description: 'find logical flows for the source and target entity references'
    },
    findUpstreamFlowsForEntityReferences: {
        serviceName,
        serviceFnName: 'findUpstreamFlowsForEntityReferences',
        description: 'findUpstreamFlowsForEntityReferences - given a list of entity reference returns all flows feeding any of those apps'
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
    addFlows: {
        serviceName,
        serviceFnName: 'addFlows',
        description: 'adds a list of logical flows'
    },
    cleanupOrphans: {
        serviceName,
        serviceFnName: 'cleanupOrphans',
        description: 'mark flows as removed if either endpoint is missing'
    },
    cleanupSelfReferences: {
        serviceName,
        serviceFnName: 'cleanupSelfReferences',
        description: 'mark flows as removed where the flow source and target are the same'
    },
};


