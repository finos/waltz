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

function service($http, base) {

    const findForUserId = (userId) =>
        $http.get(`${base}/user-contribution/user/${userId}/score`)
            .then(r => r.data);

    const findForDirects = (userId) =>
        $http.get(`${base}/user-contribution/user/${userId}/directs/score`)
            .then(r => r.data);

    const getLeaderBoard = () =>
        $http.get(`${base}/user-contribution/leader-board`)
            .then(r => r.data);

    const getLeaderBoardLastMonth = () =>
        $http.get(`${base}/user-contribution/monthly-leader-board`)
            .then(r => r.data);

    const getRankedLeaderBoard = (userId) =>
        $http.get(`${base}/user-contribution/user/${userId}/ordered-leader-board`)
            .then(r => r.data);

    return {
        findForUserId,
        findForDirects,
        getLeaderBoard,
        getLeaderBoardLastMonth,
        getRankedLeaderBoard
    };
}

service.$inject = [
    '$http',
    'BaseApiUrl'
];


export default service;
