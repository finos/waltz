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

import {checkIsIdSelector} from "../../common/checks";

function store($http, baseUrl) {

    const BASE = `${baseUrl}/roadmap`;

    const getRoadmapById = (roadmapId) =>
        $http
            .get(`${BASE}/id/${roadmapId}`)
            .then(result => result.data);

    const findRoadmapsBySelector = (selectionOptions) => {
        checkIsIdSelector(selectionOptions);
        return $http
            .post(`${BASE}/by-selector`, selectionOptions)
            .then(result => result.data);
    };

    const updateDescription = (roadmapId, newDescription) => {
        return $http
            .post(`${BASE}/id/${roadmapId}/description`, newDescription)
            .then(result => result.data);
    };

    const updateLifecycleStatus = (roadmapId, newStatus) => {
        return $http
            .post(`${BASE}/id/${roadmapId}/lifecycleStatus`, newStatus)
            .then(result => result.data);
    };

    const updateName = (roadmapId, newName) => {
        return $http
            .post(`${BASE}/id/${roadmapId}/name`, newName)
            .then(result => result.data);
    };

    const addScenario = (roadmapId, name) => {
        return $http
            .post(`${BASE}/id/${roadmapId}/add-scenario`, name)
            .then(result => result.data);
    };

    const findAllRoadmapsAndScenarios = () => {
        return $http.get(BASE)
            .then(result => result.data)
    };

    const findRoadmapsAndScenariosByRatedEntity = (ratedEntity) => {
        return $http
            .get(`${BASE}/by-rated-entity/${ratedEntity.kind}/${ratedEntity.id}`)
            .then(result => result.data);
    };

    const findRoadmapsAndScenariosByFormalRelationship = (relatedEntity) => {
        return $http
            .get(`${BASE}/by-formal-relationship/${relatedEntity.kind}/${relatedEntity.id}`)
            .then(result => result.data);
    };

    const addRoadmap = (command) => {
        return $http
            .post(BASE, command)
            .then(result => result.data);
    };


    return {
        addRoadmap,
        getRoadmapById,
        findRoadmapsBySelector,
        updateDescription,
        updateLifecycleStatus,
        updateName,
        addScenario,
        findAllRoadmapsAndScenarios,
        findRoadmapsAndScenariosByRatedEntity,
        findRoadmapsAndScenariosByFormalRelationship
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


const serviceName = "RoadmapStore";


export const RoadmapStore_API = {
    addRoadmap: {
        serviceName,
        serviceFnName: "addRoadmap",
        description: "executes addRoadmap [ command ]"
    },
    getRoadmapById: {
        serviceName,
        serviceFnName: "getRoadmapById",
        description: "executes findScenariosForRoadmap [roadmapId]"
    },
    findRoadmapsBySelector: {
        serviceName,
        serviceFnName: "findRoadmapsBySelector",
        description: "executes findRoadmapsBySelector [selectorOptions]"
    },
    updateDescription: {
        serviceName,
        serviceFnName: "updateDescription",
        description: "executes updateDescription [roadmapId, newDescription]"
    },
    updateLifecycleStatus: {
        serviceName,
        serviceFnName: "updateLifecycleStatus",
        description: "executes updateLifecycleStatus [roadmapId, newStatus]"
    },
    updateName: {
        serviceName,
        serviceFnName: "updateName",
        description: "executes updateName [roadmapId, newName]"
    },
    addScenario: {
        serviceName,
        serviceFnName: "addScenario",
        description: "executes addScenario [roadmapId, name]"
    },
    findRoadmapsAndScenariosByRatedEntity: {
        serviceName,
        serviceFnName: "findRoadmapsAndScenariosByRatedEntity",
        description: "executes findRoadmapsAndScenariosByRatedEntity [ratedEntityRef]"
    },
    findAllRoadmapsAndScenarios: {
        serviceName,
        serviceFnName: "findAllRoadmapsAndScenarios",
        description: "executes findAllRoadmapsAndScenarios []"
    },
    findRoadmapsAndScenariosByFormalRelationship: {
        serviceName,
        serviceFnName: "findRoadmapsAndScenariosByFormalRelationship",
        description: "executes findRoadmapsAndScenariosByFormalRelationship [relatedEntityRef]"
    }
};


export default {
    serviceName,
    store
};