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

        const BASE = `${BaseApiUrl}/org-unit`;


        const getById = (id) => $http
            .get(`${BASE}/${id}`)
            .then(result => result.data);


        const findAll = () => $http
            .get(BASE)
            .then(result => result.data);


        const findByIds = (ids) => $http
            .post(`${BASE}/by-ids`, ids)
            .then(result => result.data);


        const updateDescription = (id, newValue, oldValue) =>
            $http.post(`${BASE}/${id}/description`, {field: 'description', newValue, oldValue});


        const search = (query) => $http
            .get(`${BASE}/search/${query}`)
            .then(x => x.data);

        return {
            getById,
            findByIds,
            findAll,
            search,
            updateDescription
        };
    }
];

