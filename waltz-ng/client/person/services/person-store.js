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

        const BASE = `${BaseApiUrl}/person`;


        const getByEmployeeId = (empId) => $http
                .get(`${BASE}/employee-id/${empId}`)
                .then(result => result.data);


        const findByUserId = (userId) => $http
                .get(`${BASE}/user-id/${userId}`)
                .then(result => result.data);

        const getById = (id) => $http
                .get(`${BASE}/id/${id}`)
                .then(result => result.data);

        const findDirects = (empId) => $http
                .get(`${BASE}/employee-id/${empId}/directs`)
                .then(result => result.data);


        const findManagers = (empId) => $http
                .get(`${BASE}/employee-id/${empId}/managers`)
                .then(result => result.data);


        const search = (query) => $http
                .get(`${BASE}/search/${query}`)
                .then(x => x.data);


        return {
            getByEmployeeId,
            getById,
            findByUserId,
            findDirects,
            findManagers,
            search
        };
    }
];
