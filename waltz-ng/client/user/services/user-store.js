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
    const BASE = `${BaseApiUrl}/user`;

    const register = (regRequest) => $http.post(`${BASE}/new-user`, regRequest);
    const findAll = () => $http.get(BASE).then(r => r.data);
    const findForUserId = (userId) => $http.get(`${BASE}/user-id/${userId}`).then(r => r.data);
    const updateRoles = (userName, roles) => $http.post(`${BASE}/${userName}/roles`, roles).then(r => r.data);
    const deleteUser = (userName) => $http.delete(`${BASE}/${userName}`).then(r => r.data);
    const whoami = () => $http.get(`${BASE}/whoami`).then(r => r.data);

    const resetPassword = (userName, newPassword, currentPassword) => {
        const cmd = {
            userName,
            newPassword,
            currentPassword
        };
        return $http
            .post(`${BASE}/reset-password`, cmd)
            .then(r => r.data);
    };

    return {
        deleteUser,
        findAll,
        findForUserId,
        register,
        resetPassword,
        updateRoles,
        whoami
    };
}

store.$inject = ['$http', 'BaseApiUrl'];


const serviceName = 'UserStore';


export const UserStore_API = {
    deleteUser: {
        serviceName,
        serviceFnName: 'deleteUser',
        description: 'delete a user'
    },
    findAll: {
        serviceName,
        serviceFnName: 'findAll',
        description: 'find all users'
    },
    findForUserId: {
        serviceName,
        serviceFnName: 'findForUserId',
        description: 'find a user by id'
    },
    register: {
        serviceName,
        serviceFnName: 'register',
        description: 'register a new user'
    },
    resetPassword: {
        serviceName,
        serviceFnName: 'resetPassword',
        description: "reset a user's password"
    },
    updateRoles: {
        serviceName,
        serviceFnName: 'updateRoles',
        description: "update a user's permissions"
    },
    whoami: {
        serviceName,
        serviceFnName: 'whoami',
        description: "returns object representing the current user"
    }
};


export default {
    serviceName,
    store
};
