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

    const findByEntityKind = (kind, visibilites = ["PRIMARY"]) => {
        return $http
            .post(`${BASE}/entity-kind/${kind}`, visibilites)
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

    const remove = (ref, assessmentDefinitionId) => {
        checkIsEntityRef(ref);
        return $http
            .delete(`${BASE}/entity/${ref.kind}/${ref.id}/${assessmentDefinitionId}`)
            .then(d => d.data);
    };

    return {
        findForEntityReference,
        findByEntityKind,
        findByTargetKindForRelatedSelector,
        store,
        remove
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl",
];


export const serviceName = "AssessmentRatingStore";


export const AssessmentRatingStore_API = {
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
    remove: {
        serviceName,
        serviceFnName: "remove",
        description: "remove a rating"
    }
};

