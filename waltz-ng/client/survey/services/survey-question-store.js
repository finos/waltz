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

    const BASE = `${baseUrl}/survey-question`;

    const create = (q) => {
        return $http.post(`${BASE}`, q)
            .then(result => result.data);
    };

    const update = (q) => {
        return $http.put(`${BASE}`, q)
            .then(result => result.data);
    };

    const deleteQuestion = (id) => {
        return $http.delete(`${BASE}/${id}`)
            .then(result => result.data);
    };


    const findForInstance = (id) => {
        return $http.get(`${BASE}/instance/${id}`)
            .then(result => result.data);
    };

    const findForTemplate = (id) => {
        return $http.get(`${BASE}/template/${id}`)
            .then(result => result.data);
    };

    return {
        create,
        update,
        deleteQuestion,
        findForInstance,
        findForTemplate
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


const serviceName = "SurveyQuestionStore";


export const SurveyQuestionStore_API = {
    create: {
        serviceName,
        serviceFnName: "create",
        description: "create a question"
    },
    update: {
        serviceName,
        serviceFnName: "update",
        description: "updates a question"
    },
    deleteQuestion: {
        serviceName,
        serviceFnName: "deleteQuestion",
        description: "delete a question"
    },
    findForInstance: {
        serviceName,
        serviceFnName: "findForInstance",
        description: "findForInstance"
    },
    findForTemplate: {
        serviceName,
        serviceFnName: "findForTemplate",
        description: "findForTemplate"
    }
};

export default {
    store,
    serviceName
};
