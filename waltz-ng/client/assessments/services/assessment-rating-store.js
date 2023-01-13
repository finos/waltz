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


import { checkIsEntityRef } from "../../common/checks";

export function store($http, BaseApiUrl) {
    const BASE = `${BaseApiUrl}/assessment-rating`;


    const findForEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${BASE}/entity/${ref.kind}/${ref.id}`)
            .then(d => d.data);
    };

    const getRatingPermissions = (ref, definitionId) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${BASE}/entity/${ref.kind}/${ref.id}/${definitionId}/permissions`)
            .then(d => d.data);
    };

    const findByEntityKind = (kind) => {
        return $http
            .get(`${BASE}/entity-kind/${kind}`)
            .then(d => d.data);
    };

    const findByAssessmentDefinitionId = (assessmentDefinitionId) => {
        return $http
            .get(`${BASE}/definition-id/${assessmentDefinitionId}`)
            .then(d => d.data);
    };

    const findByTargetKindForRelatedSelector = (targetKind, selector) => {
        return $http
            .post(`${BASE}/target-kind/${targetKind}/selector`, selector)
            .then(r => r.data);
    };

    const store = (ref, assessmentDefinitionId, ratingId, comment = null) => {
        checkIsEntityRef(ref);
        return $http
            .post(`${BASE}/entity/${ref.kind}/${ref.id}/${assessmentDefinitionId}`, { ratingId, comment })
            .then(d => d.data);
    };

    const lock = (ref, assessmentDefinitionId, ratingId) => {
        checkIsEntityRef(ref);
        return $http
            .put(`${BASE}/entity/${ref.kind}/${ref.id}/${assessmentDefinitionId}/${ratingId}/lock`)
            .then(d => d.data);
    };

    const unlock = (ref, assessmentDefinitionId, ratingId) => {
        checkIsEntityRef(ref);
        return $http
            .put(`${BASE}/entity/${ref.kind}/${ref.id}/${assessmentDefinitionId}/${ratingId}/unlock`)
            .then(d => d.data);
    };

    const bulkStore = (assessmentDefinitionId, commands = []) => {
        return $http
            .post(`${BASE}/bulk-update/${assessmentDefinitionId}`, commands)
            .then(d => d.data);
    };

    const bulkRemove = (assessmentDefinitionId, commands = []) => {
        return $http
            .post(`${BASE}/bulk-remove/${assessmentDefinitionId}`, commands)
            .then(d => d.data);
    };

    const remove = (ref, assessmentDefinitionId, ratingId) => {
        checkIsEntityRef(ref);
        return $http
            .delete(`${BASE}/entity/${ref.kind}/${ref.id}/${assessmentDefinitionId}/${ratingId}`)
            .then(d => d.data);
    };

    return {
        getRatingPermissions,
        findForEntityReference,
        findByEntityKind,
        findByAssessmentDefinitionId,
        findByTargetKindForRelatedSelector,
        store,
        lock,
        unlock,
        bulkStore,
        bulkRemove,
        remove
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl",
];


export const serviceName = "AssessmentRatingStore";


export const AssessmentRatingStore_API = {
    getRatingPermissions: {
        serviceName,
        serviceFnName: "getRatingPermissions",
        description: "find permissions for a single rating [ref, assessmentDefId]"
    },
    findForEntityReference: {
        serviceName,
        serviceFnName: "findForEntityReference",
        description: "find all assessment ratings for an entity"
    },
    findByEntityKind: {
        serviceName,
        serviceFnName: "findByEntityKind",
        description: "find all assessment ratings for an entity kind"
    },
    findByAssessmentDefinitionId: {
        serviceName,
        serviceFnName: "findByAssessmentDefinitionId",
        description: "find all assessment ratings for an assessment definition id"
    },
    findByTargetKindForRelatedSelector: {
        serviceName,
        serviceFnName: "findByTargetKindForRelatedSelector",
        description: "find all assessment ratings for a particular target kind by a selector for that kind [targetKind, selector]"
    },
    store: {
        serviceName,
        serviceFnName: "store",
        description: "update or create a rating"
    },
    lock: {
        serviceName,
        serviceFnName: "lock",
        description: "Locks a rating [ref, defId]"
    },
    unlock: {
        serviceName,
        serviceFnName: "unlock",
        description: "Unlocks a locked rating [ref, defId]"
    },
    bulkStore: {
        serviceName,
        serviceFnName: "bulkStore",
        description: "update or create ratings in bulk for an assessment definition"
    },
    bulkRemove: {
        serviceName,
        serviceFnName: "bulkRemove",
        description: "remove ratings in bulk for an assessment definition"
    },
    remove: {
        serviceName,
        serviceFnName: "remove",
        description: "remove a rating"
    }
};

