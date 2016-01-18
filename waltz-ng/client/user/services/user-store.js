/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */

function store($http, BaseApiUrl) {
    const BASE = `${BaseApiUrl}/user`;

    const register = (regRequest) => $http.post(`${BASE}/new-user`, regRequest);
    const findAll = () => $http.get(BASE).then(r => r.data);
    const updateRoles = (userName, roles) => $http.post(`${BASE}/${userName}/roles`, roles).then(r => r.data);
    const deleteUser = (userName) => $http.delete(`${BASE}/${userName}`).then(r => r.data);
    const resetPassword = (userName, password) => $http.post(`${BASE}/${userName}/reset-password`, password).then(r => r.data);

    return {
        register,
        updateRoles,
        findAll,
        deleteUser,
        resetPassword
    };
}

store.$inject = ['$http', 'BaseApiUrl'];


export default store;
