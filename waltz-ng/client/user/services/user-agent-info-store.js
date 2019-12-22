
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

function store($http,
               $window,
               BaseApiUrl) {
    const BASE = `${BaseApiUrl}/user-agent-info`;

    const findForUser = (userName, limit = 10) => $http
        .get(`${BASE}/user/${userName}`, { params: { limit } })
        .then(r => r.data);

    const save = () => {

        const browserInfo = {
            operatingSystem: $window.navigator.platform,
            resolution: `${ $window.screen.width }x${ $window.screen.height }`
        };

        return $http
            .post(BASE, browserInfo)
            .then(r => r.data);
    };

    return {
        findForUser,
        save
    };
}

store.$inject = [
    '$http',
    '$window',
    'BaseApiUrl'
];


export default store;
