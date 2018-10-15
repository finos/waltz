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

    const BASE = `${baseUrl}/scenario`;


    const findByRoadmapSelector = (selectionOptions) => {
        checkIsIdSelector(selectionOptions);
        return $http
            .post(`${BASE}/by-roadmap-selector`, selectionOptions)
            .then(result => result.data);
    };

    const findForRoadmap = (roadmapId) =>
        $http
            .get(`${BASE}/by-roadmap-id/${roadmapId}`)
            .then(result => result.data);

    const getById = (scenarioId) =>
        $http
            .get(`${BASE}/id/${scenarioId}`)
            .then(result => result.data);

    const cloneById = (scenarioId, newName = "Clone") =>
        $http
            .post(`${BASE}/id/${scenarioId}/clone`, newName)
            .then(result => result.data);

    const removeRating = (scenarioId, appId, columnId, rowId) =>
        $http
            .delete(`${BASE}/id/${scenarioId}/rating/${appId}/${columnId}/${rowId}`)
            .then(result => result.data);

    const addRating = (scenarioId, appId, columnId, rowId, rating) =>
        $http
            .post(`${BASE}/id/${scenarioId}/rating/${appId}/${columnId}/${rowId}/${rating}`)
            .then(result => result.data);

    const updateRating = (scenarioId, appId, columnId, rowId, rating, comment) =>
        $http
            .post(`${BASE}/id/${scenarioId}/rating/${appId}/${columnId}/${rowId}/rating/${rating}`, comment)
            .then(result => result.data);

    const updateDescription = (scenarioId, newDescription) => {
        return $http
            .post(`${BASE}/id/${scenarioId}/description`, newDescription)
            .then(result => result.data);
    };

    const updateName = (scenarioId, newName) => {
        return $http
            .post(`${BASE}/id/${scenarioId}/name`, newName)
            .then(result => result.data);
    };

    const updateEffectiveDate = (scenarioId, newDate) => {
        return $http
            .post(`${BASE}/id/${scenarioId}/effective-date`, newDate)
            .then(result => result.data);
    };

    const updateScenarioType = (scenarioId, newType) => {
        return $http
            .post(`${BASE}/id/${scenarioId}/scenario-type/${newType}`)
            .then(result => result.data);
    };

    const updateReleaseStatus = (scenarioId, newStatus) => {
        return $http
            .post(`${BASE}/id/${scenarioId}/release-status/${newStatus}`)
            .then(result => result.data);
    };

    const addAxisItem = (scenarioId, orientation, domainItem, position) => {
        return $http
            .post(`${BASE}/id/${scenarioId}/axis/${orientation}/${domainItem.kind}/${domainItem.id}`, position)
            .then(result => result.data);
    };

    const removeAxisItem = (scenarioId, orientation, domainItem) => {
        return $http
            .delete(`${BASE}/id/${scenarioId}/axis/${orientation}/${domainItem.kind}/${domainItem.id}`)
            .then(result => result.data);
    };

    const loadAxis = (scenarioId, orientation) => {
        return $http
            .get(`${BASE}/id/${scenarioId}/axis/${orientation}`)
            .then(result => result.data);
    };

    const reorderAxis = (scenarioId, orientation, ids = []) => {
        return $http
            .post(`${BASE}/id/${scenarioId}/axis/${orientation}/reorder`, ids)
            .then(result => result.data);
    };

    const removeScenario = (scenarioId) => {
        return $http
            .delete(`${BASE}/id/${scenarioId}`)
            .then(result => result.data);
    };

    return {
        findForRoadmap,
        findByRoadmapSelector,
        getById,
        cloneById,
        removeRating,
        removeScenario,
        addRating,
        updateDescription,
        updateName,
        updateRating,
        updateEffectiveDate,
        updateScenarioType,
        updateReleaseStatus,
        addAxisItem,
        removeAxisItem,
        loadAxis,
        reorderAxis
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


const serviceName = "ScenarioStore";


export const ScenarioStore_API = {
    findByRoadmapSelector: {
        serviceName,
        serviceFnName: "findByRoadmapSelector",
        description: "executes findByRoadmapSelector [roadmapSelectorOptions]"
    },
    findForRoadmap: {
        serviceName,
        serviceFnName: "findForRoadmap",
        description: "executes findForRoadmap [roadmapId]"
    },
    getById: {
        serviceName,
        serviceFnName: "getById",
        description: "executes getById [scenarioId]"
    },
    cloneById: {
        serviceName,
        serviceFnName: "cloneById",
        description: "executes cloneById [scenarioId, newName]"
    },
    removeScenario: {
        serviceName,
        serviceFnName: "removeScenario",
        description: "executes removeScenario [scenarioId]"
    },
    removeRating: {
        serviceName,
        serviceFnName: "removeRating",
        description: "executes removeRating [scenarioId, appId, columnId, rowId]"
    },
    addRating: {
        serviceName,
        serviceFnName: "addRating",
        description: "executes addRating [scenarioId, appId, columnId, rowId, rating]"
    },
    updateRating: {
        serviceName,
        serviceFnName: "updateRating",
        description: "executes updateRating [scenarioId, appId, columnId, rowId, rating, comment]"
    },
    updateDescription: {
        serviceName,
        serviceFnName: "updateDescription",
        description: "executes updateDescription [scenarioId, newDescription]"
    },
    updateName: {
        serviceName,
        serviceFnName: "updateName",
        description: "executes updateName [scenarioId, newName]"
    },
    updateEffectiveDate: {
        serviceName,
        serviceFnName: "updateEffectiveDate",
        description: "executes updateEffectiveDate [scenarioId, newDate]"
    },
    updateScenarioType: {
        serviceName,
        serviceFnName: "updateScenarioType",
        description: "executes updateScenarioType [scenarioId, newStatus]"
    },
    updateReleaseStatus: {
        serviceName,
        serviceFnName: "updateReleaseStatus",
        description: "executes updateReleaseStatus [scenarioId, newStatus]"
    },
    addAxisItem: {
        serviceName,
        serviceFnName: "addAxisItem",
        description: "executes addAxisItem [scenarioId, newName]"
    },
    removeAxisItem: {
        serviceName,
        serviceFnName: "removeAxisItem",
        description: "executes removeAxisItem [scenarioId, newName]"
    },
    loadAxis: {
        serviceName,
        serviceFnName: "loadAxis",
        description: "executes loadAxis [scenarioId, orientation]"
    },
    reorderAxis: {
        serviceName,
        serviceFnName: "reorderAxis",
        description: "executes reorderAxis [scenarioId, orientation, [ids...]]"
    }
};


export default {
    serviceName,
    store
};