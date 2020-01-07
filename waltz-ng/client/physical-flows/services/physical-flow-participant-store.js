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


export function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/physical-flow-participant`;


    const findByPhysicalFlowId = (id) => {
        return $http
            .get(`${base}/physical-flow/${id}`)
            .then(r => r.data);
    };


    const findByParticipant = (ref) => {
        return $http
            .get(`${base}/participant/${ref.kind}/${ref.id}`)
            .then(r => r.data);
    };


    const remove = (physicalFlowId, kind, participant) => {
        return $http
            .delete(`${base}/physical-flow/${physicalFlowId}/${kind}/${participant.kind}/${participant.id}`)
            .then(r => r.data);
    };

    const add = (physicalFlowId, kind, participant) => {
        return $http
            .post(`${base}/physical-flow/${physicalFlowId}/${kind}/${participant.kind}/${participant.id}`)
            .then(r => r.data);
    };

    return {
        findByPhysicalFlowId,
        findByParticipant,
        remove,
        add
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


export const serviceName = "PhysicalFlowParticipantStore";



export const PhysicalFlowParticipantStore_API = {
    findByPhysicalFlowId: {
        serviceName,
        serviceFnName: "findByPhysicalFlowId",
        description: "executes findByPhysicalFlowId"
    },
    findByParticipant: {
        serviceName,
        serviceFnName: "findByParticipant",
        description: "executes findByParticipant"
    },
    remove: {
        serviceName,
        serviceFnName: "remove",
        description: "executes remove (physical flow id, kind:SOURCE|TARGET, participantRef)"
    },
    add: {
        serviceName,
        serviceFnName: "add",
        description: "executes add (physical flow id, kind:SOURCE|TARGET, participantRef)"
    }
};