/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
