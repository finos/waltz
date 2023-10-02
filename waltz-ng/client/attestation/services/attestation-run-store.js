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

function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/attestation-run`;

    const getCreateSummary = (cmd) => {
        return $http
            .post(`${base}/create-summary`, cmd)
            .then(r => r.data);
    };

    const create = (cmd) => {
        return $http
            .post(base, cmd)
            .then(r => r.data);
    };

    const getById = (id) => {
        return $http
            .get(`${base}/id/${id}`)
            .then(r => r.data);
    };

    const findAll = () => {
        return $http
            .get(`${base}`)
            .then(r => r.data);
    };

    const findByRecipient = () => {
        return $http
            .get(`${base}/user`)
            .then(r => r.data);
    };

    const findBySelector = (options) => $http
        .post(`${base}/selector`, options)
        .then(x => x.data);

    const findResponseSummaries = () => {
        return $http
            .get(`${base}/summary/response`)
            .then(r => r.data);
    };

    const findByEntityRef = (ref) => {
        checkIsEntityRef(ref);

        return $http
            .get(`${base}/entity/${ref.kind}/${ref.id}`)
            .then(r => r.data);
    };

    const findRecipientsByRunId = (id) => {
        return $http
            .get(`${base}/id/${id}/recipients`)
            .then(result => result.data);
    };

    return {
        getCreateSummary,
        create,
        getById,
        findAll,
        findByRecipient,
        findResponseSummaries,
        findByEntityRef,
        findRecipientsByRunId,
        findBySelector
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


const serviceName = "AttestationRunStore";


export const AttestationRunStore_API = {
    getCreateSummary: {
        serviceName,
        serviceFnName: "getCreateSummary",
        description: "get create summary when creating an attestation run"
    },
    create: {
        serviceName,
        serviceFnName: "create",
        description: "create an attestation run"
    },
    getById: {
        serviceName,
        serviceFnName: "getById",
        description: "attestation run by id"
    },
    findAll: {
        serviceName,
        serviceFnName: "findAll",
        description: "all attestation runs"
    },
    findByRecipient: {
        serviceName,
        serviceFnName: "findByRecipient",
        description: "attestation runs for recipient"
    },
    findResponseSummaries: {
        serviceName,
        serviceFnName: "findResponseSummaries",
        description: "attestation run response summaries"
    },
    findByEntityRef: {
        serviceName,
        serviceFnName: "findByEntityRef",
        description: "find runs for an entity"
    },
    findBySelector: {
        serviceName,
        serviceFnName: "findBySelector",
        description: "find runs for the given selector options"
    },
    findRecipientsByRunId: {
        serviceName,
        serviceFnName: "findRecipientsByRunId",
        description: "find recipients by a run id"
    }
};


export default {
    serviceName,
    store
};
