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

function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/survey-instance-view`;

    const getById = (id) => {
        return $http
            .get(`${base}/id/${id}`)
            .then(result => result.data);
    };

    return {
        getById
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


const serviceName = "SurveyInstanceViewStore";


export const SurveyInstanceViewStore_API = {
    getById: {
        serviceName,
        serviceFnName: "getById",
        description: "get survey instance info for a given id"
    }
};


export default {
    store,
    serviceName
};
