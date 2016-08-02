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
    const BASE = `${BaseApiUrl}/user-preference`;

    const findAll = (userName) => $http.get(`${BASE}/${userName}`).then(r => r.data);

    const save = (preference) => $http.post(`${BASE}/${preference.userName}/save`, preference)
        .then(r => r.data);

    const saveAllForUserName = (userName, preferences) => $http.post(`${BASE}/${userName}/save-all`, preferences)
        .then(r => r.data);

    const deleteForUserName = (userName) => $http.delete(`${BASE}/${userName}/clear`).then(r => r.data);

    return {
        findAll,
        save,
        saveAllForUserName,
        deleteForUserName
    };
}

store.$inject = ['$http', 'BaseApiUrl'];


export default store;
