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

export function store($http, base) {
    const BASE = `${base}/flow-diagram`;

    const getById = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(r => r.data);

    const deleteForId = (id) => $http
        .delete(`${BASE}/id/${id}`)
        .then(r => r.data);

    const clone = (id, newName ) => $http
        .post(`${BASE}/id/${id}/clone`, newName)
        .then(r => r.data);

    const findByEntityReference = (ref) => $http
        .get(`${BASE}/entity/${ref.kind}/${ref.id}`)
        .then(r => r.data);

    const makeNewForEntityReference = (ref, title) => $http
        .post(`${BASE}/entity/${ref.kind}/${ref.id}`, title)
        .then(r => r.data);

    const findForSelector = (options) => $http
        .post(`${BASE}/selector`, options)
        .then(r => r.data);

    // diagram -> diagramId
    const save = (diagram) => $http
        .post(BASE, diagram)
        .then(r => r.data);

    const updateName = (id, cmd) => $http
        .post(`${BASE}/update-name/${id}`, cmd)
        .then(r => r.data);

    const updateDescription = (id, cmd) => $http
        .post(`${BASE}/update-description/${id}`, cmd)
        .then(r => r.data);

    return {
        getById,
        deleteForId,
        findByEntityReference,
        makeNewForEntityReference,
        findForSelector,
        clone,
        save,
        updateName,
        updateDescription
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


const serviceName = "FlowDiagramStore";


export default {
    serviceName,
    store
};


export const FlowDiagramStore_API = {
    getById: {
        serviceName,
        serviceFnName: "getById",
        description: "getById"
    },
    deleteForId: {
        serviceName,
        serviceFnName: "deleteForId",
        description: "deleteForId"
    },
    findByEntityReference: {
        serviceName,
        serviceFnName: "findByEntityReference",
        description: "findByEntityReference"
    },
    findForSelector: {
        serviceName,
        serviceFnName: "findForSelector",
        description: "findForSelector"
    },
    save: {
        serviceName,
        serviceFnName: "save",
        description: "save"
    },
    updateName: {
        serviceName,
        serviceFnName: "updateName",
        description: "update name"
    },
    updateDescription: {
        serviceName,
        serviceFnName: "updateDescription",
        description: "update description"
    },
    clone: {
        serviceName,
        serviceFnName: "clone",
        description: "clone [title?]"
    },
    makeNewForEntityReference: {
        serviceName,
        serviceFnName: "makeNewForEntityReference",
        description: "makeNewForEntityReference [ref, title?]"
    }
};