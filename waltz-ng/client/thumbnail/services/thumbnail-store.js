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


import { checkIsEntityRef } from "../../common/checks";

function store($http, BaseApiURL) {
    const BASE = `${BaseApiURL}/thumbnail`;

    const getByReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${BASE}/${ref.kind}/${ref.id}`)
            .then(d => d.data);
    };


    const deleteByReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .delete(`${BASE}/${ref.kind}/${ref.id}`)
            .then(d => d.data);
    };


    const save = (cmd) => {
        return $http
            .post(`${BASE}/save`, cmd)
            .then(d => d.data);
    };

    return {
        getByReference,
        deleteByReference,
        save
    }
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


const serviceName = "ThumbnailStore";


export const ThumbnailStore_API = {
    getByReference: {
        serviceName,
        serviceFnName: "getByReference",
        description: "get a thumbnail by entity ref"
    },
    deleteByReference: {
        serviceName,
        serviceFnName: "deleteByReference",
        description: "delete a thumbnail by entity ref"
    },
    save: {
        serviceName,
        serviceFnName: "save",
        description: "save a thumbnail"
    }
};


export default {
    serviceName,
    store
};