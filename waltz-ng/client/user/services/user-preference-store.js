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

    const findAllForUser = () => $http.get(`${BASE}`).then(r => r.data);

    /**
     * preference -> [ preferences... ]
     * @param preference
     */
    const saveForUser = (preference) => $http.post(`${BASE}/save`, preference)
        .then(r => r.data);

    /**
     * [ preferences ... ] -> [ preferences... ]
     * @param preferences
     */
    const saveAllForUser = (preferences) => $http.post(`${BASE}/save-all`, preferences)
        .then(r => r.data);

    const deleteForUser = () => $http.delete(`${BASE}/clear`).then(r => r.data);

    return {
        findAllForUser,
        saveForUser,
        saveAllForUser,
        deleteForUser
    };
}

store.$inject = ['$http', 'BaseApiUrl'];


export default store;
