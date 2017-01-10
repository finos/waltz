
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

let userPromise = null;
let userName = null;

function service(http, baseUrl, $auth) {


    const BASE = `${baseUrl}/user`;


    const whoami = (force = false) => {
        if (force || (userPromise == null && userName == null)) {
            userPromise = http.get(`${BASE}/whoami`)
                .then(result => result.data)
                .then(u => {
                    userName = u.userName === 'anonymous'
                        ? null
                        : u.userName;
                    return u;
                });
        }
        return userPromise;
    };


    const login = (credentials) =>
        $auth.login(credentials)
            .then((response) => {
                $auth.setToken(response.data.token);
            });


    const logout = () =>
        $auth
            .logout()
            .then(() => {
                this.userPromise = null;
                this.user = null
            });


    const hasRole = ( { roles = [] }, role) => {
        return _.includes(roles, role);
    };


    whoami();

    return {
        whoami,
        login,
        logout,
        hasRole
    };
}

service.$inject = [
    '$http',
    'BaseApiUrl',
    '$auth'
];


export default service;
