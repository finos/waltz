/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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