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
    const BASE = `${base}/flow-diagram-overlay-group`;

    const findByDiagramId = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(r => r.data);

    const findOverlaysByDiagramId = (id) => $http
        .get(`${BASE}/overlays/diagram-id/${id}`)
        .then(r => r.data);

    return {
        findByDiagramId,
        findOverlaysByDiagramId
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


const serviceName = "FlowDiagramOverlayGroupStore";


export default {
    serviceName,
    store
};


export const FlowDiagramOverlayGroupStore_API = {
    findByDiagramId: {
        serviceName,
        serviceFnName: "findByDiagramId",
        description: "findByDiagramId"
    },
    findOverlaysByDiagramId: {
        serviceName,
        serviceFnName: "findOverlaysByDiagramId",
        description: "findOverlaysByDiagramId"
    },
};