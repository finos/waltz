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
import { checkIsEntityInvolvementChangeCommand } from "../../common/checks";


function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/involvement`;


    const findByEmployeeId = (employeeId) =>
        $http.get(`${BASE}/employee/${employeeId}`)
            .then(result => result.data);


    const findByEntityReference = (kind, id) => {
        const ref = _.isObject(kind) ? kind : { id, kind };

        return $http.get(`${BASE}/entity/${ref.kind}/${ref.id}`)
            .then(result => result.data);
    };

    const findBySelector = (selectorOptions) => {
        return $http
            .post(`${BASE}/selector/involvement`, selectorOptions)
            .then(result => result.data);
    };


    const findPeopleByEntityReference = (kind, id) => {
        const ref = _.isObject(kind) ? kind : {id, kind};

        return $http.get(`${BASE}/entity/${ref.kind}/${ref.id}/people`)
            .then(result => result.data);
    };


    const findExistingInvolvementKindIdsForUser = (kind, id) => {
        const ref = _.isObject(kind) ? kind : {id, kind};

        return $http.get(`${BASE}/entity/${ref.kind}/${ref.id}/user`)
            .then(result => result.data);
    };


    const findPeopleBySelector = (selectorOptions) => {
        return $http
            .post(`${BASE}/selector/people`, selectorOptions)
            .then(result => result.data);
    };


    const changeInvolvement = (entityRef, cmd) => {
        checkIsEntityInvolvementChangeCommand(cmd);
        return $http
            .post(`${BASE}/entity/${entityRef.kind}/${entityRef.id}`, cmd)
            .then(r => r.data);
    };


    const countOrphanInvolvementsForKind = (entityKind) => {
        return $http
            .get(`${BASE}/entity-kind/${entityKind}/orphan-count`, entityKind)
            .then(r => r.data);
    };


    const cleanupOrphansForKind = (entityKind) => {
        return $http
            .delete(`${BASE}/entity-kind/${entityKind}/cleanup-orphans`, entityKind)
            .then(r => r.data);
    };


    return {
        findByEmployeeId,
        findByEntityReference,
        findBySelector,
        findPeopleByEntityReference,
        findPeopleBySelector,
        findExistingInvolvementKindIdsForUser,
        changeInvolvement,
        countOrphanInvolvementsForKind,
        cleanupOrphansForKind
    };
}


store.$inject = ["$http", "BaseApiUrl"];


const serviceName = "InvolvementStore";


export const InvolvementStore_API = {
    findByEmployeeId: {
        serviceName,
        serviceFnName: "findByEmployeeId",
        description: "find involvements by employee id"
    },
    findByEntityReference: {
        serviceName,
        serviceFnName: "findByEntityReference",
        description: "find involvements by entity reference"
    },
    findExistingInvolvementKindIdsForUser: {
        serviceName,
        serviceFnName: "findExistingInvolvementKindIdsForUser",
        description: "find involvement kind ids by entity reference for user"
    },
    findBySelector: {
        serviceName,
        serviceFnName: "findBySelector",
        description: "find involvements by entity selector"
    },
    findPeopleByEntityReference: {
        serviceName,
        serviceFnName: "findPeopleByEntityReference",
        description: "find people by involved entity reference"
    },
    findPeopleBySelector: {
        serviceName,
        serviceFnName: "findPeopleBySelector",
        description: "find people by generic selector"
    },
    changeInvolvement: {
        serviceName,
        serviceFnName: "changeInvolvement",
        description: "change person involvement for a given entity reference"
    },
    countOrphanInvolvementsForKind: {
        serviceName,
        serviceFnName: "countOrphanInvolvementsForKind",
        description: "count of involvements where entity no longer exists for a given entity kind"
    },
    cleanupOrphansForKind: {
        serviceName,
        serviceFnName: "cleanupOrphansForKind",
        description: "cleanup involvements for a given entity kind"
    }
};


export default {
    store,
    serviceName
};
