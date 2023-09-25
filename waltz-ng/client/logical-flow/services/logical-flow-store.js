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

import _ from "lodash";
import {checkIsEntityRef, checkIsIdSelector} from "../../common/checks"

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


    const findByIds = (ids = []) => {
        return $http
            .post(`${BASE}/ids`, ids)
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

    const restoreFlow = (id) => $http
        .put(`${BASE}/${id}/restore`)
        .then(r => r.data);

    const addFlow = (addFlowCmd) => $http
        .post(`${BASE}`, addFlowCmd)
        .then(r => r.data);

    const addFlows = (addFlowCmds) => $http
        .post(`${BASE}/list`, addFlowCmds)
        .then(r => r.data);

    const findPermissionsForParentRef = (ref) => $http
        .get(`${BASE}/entity/${ref.kind}/${ref.id}/permissions`)
        .then(r => r.data);

    const findPermissionsForFlow = (id) => $http
        .get(`${BASE}/id/${id}/permissions`)
        .then(r => r.data);

    const cleanupOrphans = () => $http
        .get(`${BASE}/cleanup-orphans`)
        .then(r => r.data);

    const cleanupSelfReferences = () => $http
        .get(`${BASE}/cleanup-self-references`)
        .then(r => r.data);

    return {
        findBySelector,
        findByIds,
        findByEntityReference,
        findBySourceAndTargetEntityReferences,
        findUpstreamFlowsForEntityReferences,
        calculateStats,
        countByDataType,
        removeFlow,
        restoreFlow,
        getById,
        addFlow,
        addFlows,
        findPermissionsForParentRef,
        findPermissionsForFlow,
        cleanupOrphans,
        cleanupSelfReferences
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];

const serviceName = "LogicalFlowStore";

export default {
    serviceName,
    store
};

export const LogicalFlowStore_API = {
    findByIds: {
        serviceName,
        serviceFnName: "findByIds",
        description: "find logical flows for a set of ids"
    },
    findBySelector: {
        serviceName,
        serviceFnName: "findBySelector",
        description: "find logical flows for a selector"
    },
    findByEntityReference: {
        serviceName,
        serviceFnName: "findByEntityReference",
        description: "find logical flows involving a given entity"
    },
    findBySourceAndTargetEntityReferences: {
        serviceName,
        serviceFnName: "findBySourceAndTargetEntityReferences",
        description: "find logical flows for the source and target entity references"
    },
    findUpstreamFlowsForEntityReferences: {
        serviceName,
        serviceFnName: "findUpstreamFlowsForEntityReferences",
        description: "findUpstreamFlowsForEntityReferences - given a list of entity reference returns all flows feeding any of those apps"
    },
    calculateStats: {
        serviceName,
        serviceFnName: "calculateStats",
        description: "calculate statistics for flows"
    },
    countByDataType: {
        serviceName,
        serviceFnName: "countByDataType",
        description: "summarizes flows by their data types"
    },
    removeFlow: {
        serviceName,
        serviceFnName: "removeFlow",
        description: "removes a single logical flow"
    },
    restoreFlow: {
        serviceName,
        serviceFnName: "restoreFlow",
        description: "restore a single logical flow"
    },
    getById: {
        serviceName,
        serviceFnName: "getById",
        description: "retrieve a single logical flow (or null) given an id"
    },
    addFlow: {
        serviceName,
        serviceFnName: "addFlow",
        description: "adds a single logical flow"
    },
    addFlows: {
        serviceName,
        serviceFnName: "addFlows",
        description: "adds a list of logical flows"
    },
    findPermissionsForParentRef: {
        serviceName,
        serviceFnName: "findPermissionsForParentRef",
        description: "returns the operations that a user can perform on flows linked to the parent entity"
    },
    findPermissionsForFlow: {
        serviceName,
        serviceFnName: "findPermissionsForFlow",
        description: "returns the operations that a user can perform on a flow"
    },
    cleanupOrphans: {
        serviceName,
        serviceFnName: "cleanupOrphans",
        description: "mark flows as removed if either endpoint is missing"
    },
    cleanupSelfReferences: {
        serviceName,
        serviceFnName: "cleanupSelfReferences",
        description: "mark flows as removed where the flow source and target are the same"
    },
};


