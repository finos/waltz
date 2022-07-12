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

function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/data-types`;

    const findAll = () =>
        $http.get(BASE)
            .then(result => result.data);


    const search = (query) => $http
        .post(`${BASE}/search`, query)
        .then(r => r.data);

    const getDataTypeById = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(r => r.data);

    const getDataTypeByCode = (code) => $http
        .get(`${BASE}/code/${code}`)
        .then(r => r.data);

    const findSuggestedByEntityRef = (ref) => $http
        .get(`${BASE}/suggested/entity/${ref.kind}/${ref.id}`)
        .then(r => r.data);

    return {
        findAll,
        search,
        getDataTypeById,
        getDataTypeByCode,
        findSuggestedByEntityRef
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


const serviceName = "DataTypeStore";


export const DataTypeStore_API = {
    findAll: {
        serviceName,
        serviceFnName: "findAll",
        description: "finds all data types"
    },
    search: {
        serviceName,
        serviceFnName: "search",
        description: "search data types"
    },
    getDataTypeById: {
        serviceName,
        serviceFnName: "getDataTypeById",
        description: "get datatype by id"
    },
    getDataTypeByCode: {
        serviceName,
        serviceFnName: "getDataTypeByCode",
        description: "get datatype by code"
    },
    findSuggestedByEntityRef: {
        serviceName,
        serviceFnName: "findSuggestedByEntityRef",
        description: "find suggested types based on the given [ref]"
    }
};


export default {
    store,
    serviceName
};
