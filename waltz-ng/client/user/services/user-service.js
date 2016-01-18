
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

function service(http, baseUrl, $auth, $q) {

    const BASE = `${baseUrl}/user`;

    const whoami = (force) => {
        if (force || !this.user) {
            return http.get(`${BASE}/whoami`)
                .then(result => {
                    this.user = result.data;
                    return this.user;
                });
        } else {
            return $q((resolve) => resolve(this.user));
        }
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


    whoami(true);

    return {
        whoami,
        login,
        logout
    };
}

service.$inject = ['$http', 'BaseApiUrl', '$auth', '$q'];


export default service;
