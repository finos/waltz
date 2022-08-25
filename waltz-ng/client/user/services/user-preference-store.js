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
    const BASE = `${BaseApiUrl}/user-preference`;

    const findAllForUser = () => $http
        .get(`${BASE}`)
        .then(r => r.data);

    /**
     * preference -> [ preferences... ]
     * @param preference
     */
    const saveForUser = (preference) => $http
        .post(`${BASE}/save`, preference)
        .then(r => r.data);

    /**
     * [ preferences ... ] -> [ preferences... ]
     * @param preferences
     */
    const saveAllForUser = (preferences) => $http
        .post(`${BASE}/save-all`, preferences)
        .then(r => r.data);

    const deleteForUser = () => $http
        .delete(`${BASE}/clear`)
        .then(r => r.data);

    return {
        findAllForUser,
        saveForUser,
        saveAllForUser,
        deleteForUser
    };
}

store.$inject = ["$http", "BaseApiUrl"];

const serviceName = "UserPreferenceStore";

export default {
    store,
    serviceName
}

export const UserPreferenceStore_API = {
    findAllForUser: {
        serviceName,
        serviceFnName: "findAllForUser",
        description: "executes findAllForUser"
    },
    saveForUser: {
        serviceName,
        serviceFnName:"saveForUser",
        description: "executes saveForUser"
    },
    saveAllForUser: {
        serviceName,
        serviceFnName: "saveAllForUser",
        description: "executes saveAllForUser"
    },
    deleteForUser: {
        serviceName,
        serviceFnName:"deleteForUser",
        description: "executes deleteForUser"
    }
};

