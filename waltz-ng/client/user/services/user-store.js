/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

function store($http, BaseApiUrl) {
    const BASE = `${BaseApiUrl}/user`;

    const register = (regRequest) => $http.post(`${BASE}/new-user`, regRequest);
    const findAll = () => $http.get(BASE).then(r => r.data);
    const findForUserId = (userId) => $http.get(`${BASE}/user-id/${userId}`).then(r => r.data);
    const updateRoles = (userName, roles) => $http.post(`${BASE}/${userName}/roles`, roles).then(r => r.data);
    const deleteUser = (userName) => $http.delete(`${BASE}/${userName}`).then(r => r.data);
    const resetPassword = (userName, password) => $http.post(`${BASE}/${userName}/reset-password`, password).then(r => r.data);

    return {
        deleteUser,
        findAll,
        findForUserId,
        register,
        resetPassword,
        updateRoles
    };
}

store.$inject = ['$http', 'BaseApiUrl'];


export default store;
