
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

let userPromise = null;
let userName = null;

function service(http, baseUrl, $auth) {


    const BASE = `${baseUrl}/user`;


    const whoami = (force = false) => {
        console.log('whoami2', force, userPromise == null, userName == null, '=>', force || (userPromise == null && userName == null))
        if (force || (userPromise == null && userName == null)) {
            console.log("Aarghhhh!")
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
