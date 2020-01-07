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

    const base = `${baseApiUrl}/external-identifier`;

    const findByEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${base}/entity/${ref.kind}/${ref.id}`)
            .then(r => r.data);
    };

    const deleteExternalIdentifier = (ref, id, system) => $http
        .delete(`${base}/entity/${ref.kind}/${ref.id}/${system}/externalId/${id}`)
        .then(r => r.data);

    const addExternalIdentifier = (ref, id) => $http
        .post(`${base}/entity/${ref.kind}/${ref.id}/externalId/${id}`)
        .then(r => r.data);

    return {
        findByEntityReference,
        addExternalIdentifier,
        deleteExternalIdentifier
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


export const serviceName = "ExternalIdentifierStore";


export const ExternalIdentifierStore_API = {
    findByEntityReference: {
        serviceName,
        serviceFnName: "findByEntityReference",
        description: "executes findByEntityReference"
    },
    addExternalIdentifier: {
        serviceName,
        serviceFnName: "addExternalIdentifier",
        description: "executes addExternalIdentifier"
    },
    deleteExternalIdentifier: {
        serviceName,
        serviceFnName: "deleteExternalIdentifier",
        description: "executes deleteExternalIdentifier"
    }
};