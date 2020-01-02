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

function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/entity-hierarchy`;

    const findTallies = () =>
        $http
            .get(`${BASE}/tallies`)
            .then(r => r.data);

    const findRootTallies = () =>
        $http
            .get(`${BASE}/root-tallies`)
            .then(r => r.data);

    const findRoots = (kind) =>
        $http
            .get(`${BASE}/roots/${kind}`)
            .then(r => r.data);

    const buildForKind = (kind) =>
        $http
            .post(`${BASE}/build/${kind}`, {})
            .then(r => r.data);

    return {
        findTallies,
        findRootTallies,
        findRoots,
        buildForKind
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;
