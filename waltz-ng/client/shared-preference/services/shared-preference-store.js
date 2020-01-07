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
import {checkIsEntityRef} from "../../common/checks";

function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/shared-preference`;

    const getByKeyAndCategory = (key, category) => {
        const body = {
            key,
            category
        };
        return $http
            .post(`${base}/key-category`, body)
            .then(result => result.data);
    };

    const findByCategory = (category) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${base}/category/${category}`)
            .then(r => r.data);
    };

    const generateKeyRoute = (obj) => {
        return $http
            .post(`${base}/generate-key`, obj)
            .then(result => result.data);
    };


    const save = (key, category, value) => {
        const cmd = {
            key,
            category,
            value: value
        };
        return $http
            .post(`${base}/save`, cmd)
            .then(result => result.data);
    };

    return {
        getByKeyAndCategory,
        findByCategory,
        generateKeyRoute,
        save
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


const serviceName = 'SharedPreferenceStore';


export const SharedPreferenceStore_API = {
    getByKeyAndCategory: {
        serviceName,
        serviceFnName: 'getByKeyAndCategory',
        description: 'executes getByKeyAndCategory'
    },
    findByCategory: {
        serviceName,
        serviceFnName: 'findByCategory',
        description: 'executes findByCategory'
    },
    generateKeyRoute: {
        serviceName,
        serviceFnName: 'generateKeyRoute',
        description: 'executes generateKeyRoute'
    },
    save: {
        serviceName,
        serviceFnName: 'save',
        description: 'executes save'
    },
};


export default {
    store,
    serviceName
};
