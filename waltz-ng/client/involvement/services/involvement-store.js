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

        const BASE = `${BaseApiUrl}/involvement`;


        const findAppsForEmployeeId = (employeeId) =>
            $http.get(`${BASE}/employee/${employeeId}/applications`)
                .then(result => result.data);


        const findEndUserAppsBydSelector = (options) => $http
            .post(`${BASE}/end-user-application`, options)
            .then(r => r.data);


        const findChangeInitiativesForEmployeeId = (employeeId) =>
            $http.get(`${BASE}/employee/${employeeId}/change-initiative/direct`)
                .then(result => result.data);


        const findByEmployeeId = (employeeId) =>
            $http.get(`${BASE}/employee/${employeeId}`)
                .then(result => result.data);


        const findByEntityReference = (kind, id) =>
            $http.get(`${BASE}/entity/${kind}/${id}`)
                .then(result => result.data);


        const findPeopleByEntityReference = (kind, id) =>
            $http.get(`${BASE}/entity/${kind}/${id}/people`)
                .then(result => result.data);


        return {
            findAppsForEmployeeId,
            findEndUserAppsBydSelector,
            findByEmployeeId,
            findByEntityReference,
            findChangeInitiativesForEmployeeId,
            findPeopleByEntityReference
        };
    }
];
