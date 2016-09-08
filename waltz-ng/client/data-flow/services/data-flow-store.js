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

function service($http, BaseApiUrl) {
    const BASE = `${BaseApiUrl}/data-flows`;

    // --- FINDERS ---
    const findBySelector = (options) => $http
        .post(`${BASE}/selector`, options)
        .then(r => r.data);

    const findByEntityReference = (kind, id) => $http
        .get(`${BASE}/entity/${kind}/${id}`)
        .then(result => result.data);


    // --- STATS ---
    const calculateStats = (options) => $http
        .post(`${BASE}/stats`, options)
        .then(r => r.data);

    const countByDataType = () => $http
        .get(`${BASE}/count-by/data-type`)
        .then(result => result.data);

    // --- UPDATERS ---
    const removeFlow = (id) => $http
        .delete(`${BASE}/${id}`)
        .then(r => r.data);

    const addFlow = (flow) => $http
        .post(`${BASE}`, flow)
        .then(r => r.data);

    return {
        findBySelector,
        findByEntityReference,
        calculateStats,
        countByDataType,
        removeFlow,
        addFlow
    };
}


service.$inject = [
    '$http',
    'BaseApiUrl'
];


export default service;
