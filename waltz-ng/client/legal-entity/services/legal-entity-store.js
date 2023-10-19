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


export function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/legal-entity`;


    const getById = (id) =>
        $http
            .get(`${BASE}/id/${id}`)
            .then(result => result.data);

    const findAll = () => {
        return $http
            .get(`${BASE}/all`)
            .then(result => result.data);
    };

    const findBySelector = (selector, force = false) => {
        checkIsIdSelector(selector);
        return $http
            .post(`${BASE}/selector`, selector)
            .then(result => result.data);
    };

    return {
        getById,
        findAll,
        findBySelector
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


export const serviceName = "LegalEntityStore";


export const LegalEntityStore_API = {
    getById: {
        serviceName,
        serviceFnName: "getById",
        description: "executes getById"
    },
    findAll: {
        serviceName,
        serviceFnName: "findAll",
        description: "executes findAll"
    },
    findBySelector: {
        serviceName,
        serviceFnName: "findBySelector",
        description: "executes findBySelector"
    }
};

export default {
    serviceName,
    store
};
