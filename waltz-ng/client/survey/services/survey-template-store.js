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

function store($http, baseUrl) {

    const BASE = `${baseUrl}/survey-template`;

    const create = (cmd) => {
        return $http.post(`${BASE}`, cmd)
            .then(result => result.data);
    };

    const clone = (id) => {
        return $http.post(`${BASE}/${id}/clone`)
            .then(result => result.data);
    };

    const getById = (id) => {
        return $http.get(`${BASE}/${id}`)
            .then(result => result.data);
    };

    const findAll = () => {
        return $http.get(`${BASE}`)
            .then(result => result.data);
    };

    const findByOwner = () => {
        return $http.get(`${BASE}/owner`)
            .then(result => result.data);
    };

    const update = (cmd) => {
        return $http.put(`${BASE}`, cmd)
            .then(result => result.data);
    };

    const updateStatus = (id, cmd) => {
        return $http.put(`${BASE}/${id}/status`, cmd)
            .then(result => result.data);
    };

    const remove = (id) => {
        return $http
            .delete(`${BASE}/${id}`)
            .then(result => result.data);
    }


    return {
        create,
        clone,
        getById,
        findAll,
        findByOwner,
        update,
        updateStatus,
        remove
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


const serviceName ="SurveyTemplateStore";


export const SurveyTemplateStore_API = {
    create: {
        serviceName,
        serviceFnName: "create",
        description: "create survey template"
    },
    clone: {
        serviceName,
        serviceFnName: "clone",
        description: "clone survey template"
    },
    getById: {
        serviceName,
        serviceFnName: "getById",
        description: "get survey template for a given id"
    },
    findAll: {
        serviceName,
        serviceFnName: "findAll",
        description: "find all survey templates"
    },
    update: {
        serviceName,
        serviceFnName: "update",
        description: "update a survey template"
    },
    updateStatus: {
        serviceName,
        serviceFnName: "updateStatus",
        description: "update a survey template's status"
    },
    remove: {
        serviceName,
        serviceFnName: "remove",
        description: "remove a survey templates"
    }
};


export default {
    store,
    serviceName
};
