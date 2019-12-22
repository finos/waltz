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
import {checkIsEntityRelationshipChangeCommand, checkIsIdSelector} from "../../common/checks";


function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/change-initiative`;


    const findBySelector = (selector) => {
        checkIsIdSelector(selector);
        return $http
            .post(`${BASE}/selector`, selector)
            .then(r => r.data);
    };


    const findByExternalId = (extId) => {
        return $http
            .get(`${BASE}/external-id/${extId}`)
            .then(r => r.data);
    };


    const findHierarchyBySelector = (selector) => {
        checkIsIdSelector(selector);
        return $http
            .post(`${BASE}/hierarchy/selector`, selector)
            .then(r => r.data);
    };


    const getById = (id) => $http
            .get(`${BASE}/id/${id}`)
            .then(r => r.data);


    const findRelatedForId = (id) => $http
            .get(`${BASE}/id/${id}/related`)
            .then(r => r.data);


    const search = (query) => $http
            .get(`${BASE}/search/${query}`)
            .then(r => r.data);


    const changeRelationship = (id, cmd) => {
        checkIsEntityRelationshipChangeCommand(cmd);
        return $http
            .post(`${BASE}/id/${id}/entity-relationship`, cmd)
            .then(r => r.data);
    };


    return {
        findBySelector,
        findByExternalId,
        findHierarchyBySelector,
        findRelatedForId,
        getById,
        search,
        changeRelationship
    }
}

store.$inject = ["$http", "BaseApiUrl"];


const serviceName = "ChangeInitiativeStore";


export const ChangeInitiativeStore_API = {
    findBySelector: {
        serviceName,
        serviceFnName: "findBySelector",
        description: "finds change initiatives by id selector"
    },
    findByExternalId: {
        serviceName,
        serviceFnName: "findByExternalId",
        description: "finds change initiatives by external id"
    },
    findHierarchyBySelector: {
        serviceName,
        serviceFnName: "findHierarchyBySelector",
        description: "finds change initiatives (and parents) by id selector"
    },
    getById: {
        serviceName,
        serviceFnName: "getById",
        description: "get a change initiative by id"
    },
    findRelatedForId: {
        serviceName,
        serviceFnName: "findRelatedForId",
        description: "find related change initiatives related to one with supplied id"
    },
    search: {
        serviceName,
        serviceFnName: "search",
        description: "search change initiatives"
    },
    changeRelationship: {
        serviceName,
        serviceFnName: "changeRelationship",
        description: "change relationship between a change initiative and entity reference"
    }
};


export default {
    store,
    serviceName
};