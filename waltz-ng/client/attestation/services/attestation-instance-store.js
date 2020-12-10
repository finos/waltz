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

import {checkIsEntityRef} from "../../common/checks";

export function store($http, baseApiUrl) {
    const base = `${baseApiUrl}/attestation-instance`;

    const attestInstance = (id) => {
        return $http
            .post(`${base}/attest/${id}`);
    };

    const attestEntityForUser = (cmd) => {
        return $http
            .post(`${base}/attest-entity`, cmd)
            .then(r => r.data);
    };

    const findByRunId = (id) => {
        return $http
            .get(`${base}/run/${id}`)
            .then(result => result.data);
    };

    const findByUser = (all = false) => {
        const filter = all ? 'all' : 'unattested';
        return $http
            .get(`${base}/${filter}/user`)
            .then(result => result.data);
    };

    const findHistoricalForPendingByUser = () => {
        return $http
            .get(`${base}/historical/user`)
            .then(result => result.data);
    };

    const findPersonsById = (id) => {
        return $http
            .get(`${base}/${id}/person`)
            .then(result => result.data);
    };

    const findByEntityRef = (ref) => {
        checkIsEntityRef(ref);

        return $http
            .get(`${base}/entity/${ref.kind}/${ref.id}`)
            .then(r => r.data);
    };

    const findBySelector = (options) => $http
        .post(`${base}/selector`, options)
        .then(x => x.data);

    const cleanupOrphans = () => {
        return $http
            .get(`${base}/cleanup-orphans`)
            .then(r => r.data);
    };

    return {
        attestInstance,
        attestEntityForUser,
        findByRunId,
        findByUser,
        findHistoricalForPendingByUser,
        findPersonsById,
        findByEntityRef,
        findBySelector,
        cleanupOrphans
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = 'AttestationInstanceStore';


export const AttestationInstanceStore_API = {
    attestInstance: {
        serviceName,
        serviceFnName: 'attestInstance',
        description: 'create an attestation'
    },
    attestEntityForUser: {
        serviceName,
        serviceFnName: 'attestEntityForUser',
        description: 'attest existing/create new attestation for user'
    },
    findByRunId: {
        serviceName,
        serviceFnName: 'findByRunId',
        description: 'find attestations by a run id'
    },
    findByUser: {
        serviceName,
        serviceFnName: 'findByUser',
        description: 'find attestations for a user'
    },
    findHistoricalForPendingByUser: {
        serviceName,
        serviceFnName: 'findHistoricalForPendingByUser',
        description: 'find historical attestations for pending attestations for which this user has to attest'
    },
    findPersonsById: {
        serviceName,
        serviceFnName: 'findPersonsById',
        description: 'find recipients (person) for an instance'
    },
    findByEntityRef: {
        serviceName,
        serviceFnName: 'findByEntityRef',
        description: 'find instances for an entity'
    },
    findBySelector: {
        serviceName,
        serviceFnName: "findBySelector",
        description: "find instances for the given selector options"
    },
    cleanupOrphans: {
        serviceName,
        serviceFnName: 'cleanupOrphans',
        description: 'clean up orphan attestations and recipients for applications that no longer exist'
    }
};