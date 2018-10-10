
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
import _ from "lodash";


let userPromise = null;
let userName = null;


function service(http, baseUrl, $auth) {


    const BASE = `${baseUrl}/user`;


    const whoami = (force = false) => {
        if (force || (userPromise == null && userName == null)) {
            userPromise = http.get(`${BASE}/whoami`)
                .then(result => result.data)
                .then(u => {
                    userName = u.userName === "anonymous"
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


    /**
     * @param user object with roles array property
     * @param role - either obj w/ key or string
     * @returns {boolean}
     */
    const hasRole = ( { roles = [] }, role) => {
        if (_.isEmpty(role)) return true;
        const roleToCheck = _.isObject(role) ? role.key : role;
        return _.includes(roles, roleToCheck);
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
    "$http",
    "BaseApiUrl",
    "$auth"
];


export default service;
