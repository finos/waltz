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
