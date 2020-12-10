
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
     * @param role - either obj w/ `key` or plain string
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
