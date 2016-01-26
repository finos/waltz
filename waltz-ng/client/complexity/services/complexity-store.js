
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

export default [
    '$http',
    'BaseApiUrl',
    ($http, BaseApiUrl) => {

        const BASE = `${BaseApiUrl}/complexity`;


        const findByApplication = id =>
            $http.get(`${BASE}/application/${id}`)
                .then(result => result.data);


        const findByOrgUnitTree = (id) =>
            $http.get(`${BASE}/org-unit/${id}`)
                .then(ts => {
                    return ts;
                })
                .then(result => result.data);


        const findByOrgUnit = (id) =>
            $http.get(`${BASE}/org-unit/${id}`)
                .then(ts => {
                    return ts;
                })
                .then(result => result.data.filter(c => c.id === id));


        return {
            findByOrgUnit,
            findByOrgUnitTree,
            findByApplication
        };
    }
];
