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

export function store($http, base) {
    const BASE = `${base}/flow-diagram-entity`;

    const findByDiagramId = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(r => r.data);

    const findByEntityReference = (ref) => $http
        .get(`${BASE}/entity/${ref.kind}/${ref.id}`)
        .then(r => r.data);

    const findForSelector = (options) => $http
        .post(`${BASE}/selector`, options)
        .then(r => r.data);

    const addRelationship = (diagramId, ref) => {
        checkIsEntityRef(ref);
        return $http
            .post(`${BASE}/id/${diagramId}/${ref.kind}/${ref.id}`, {})
            .then(r => r.data);
    };

    const removeRelationship = (diagramId, ref) => {
        checkIsEntityRef(ref);
        return $http
            .delete(`${BASE}/id/${diagramId}/${ref.kind}/${ref.id}`)
            .then(r => r.data);
    };


    return {
        findByDiagramId,
        findByEntityReference,
        findForSelector,
        addRelationship,
        removeRelationship
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


const serviceName = 'FlowDiagramEntityStore';


export default {
    serviceName,
    store
};


export const FlowDiagramEntityStore_API = {
    findByDiagramId: {
        serviceName,
        serviceFnName: 'findByDiagramId',
        description: 'findByDiagramId'
    },
    findByEntityReference: {
        serviceName,
        serviceFnName: 'findByEntityReference',
        description: 'findByEntityReference'
    },
    findForSelector: {
        serviceName,
        serviceFnName: 'findForSelector',
        description: 'findForSelector'
    },
    addRelationship: {
        serviceName,
        serviceFnName: 'addRelationship',
        description: 'addRelationship (diagramId, ref)'
    },
    removeRelationship: {
        serviceName,
        serviceFnName: 'removeRelationship',
        description: 'removeRelationship (diagramId, ref)'
    }
};