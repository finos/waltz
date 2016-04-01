
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

function service(http, baseUrl, $auth, $q) {


    const BASE = `${baseUrl}/user`;


    const whoami = (force = false) => {
        if (force || userPromise == null) {
            userPromise = http.get(`${BASE}/whoami`)
                .then(result => {
                    return result.data;
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
            .then(() => this.user = null);


    const hasRole = ( { roles = [] }, role) => {
        return _.contains(roles, role);
    };


    whoami(true);

    return {
        whoami,
        login,
        logout,
        hasRole
    };
}



service.$inject = ['$http', 'BaseApiUrl', '$auth', '$q'];


export default service;
