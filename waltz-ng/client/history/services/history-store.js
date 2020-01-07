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

import _ from "lodash";

const key = "history_2";

function store(storage) {

    let all = storage.get(key);

    function put(name, kind, state, stateParams) {
        const history = storage.get(key) || [];
        const item = { name, state, kind, stateParams };

        const newHistory = _.chain([item, ...history])
            .uniqBy(h => JSON.stringify(h))
            .take(16)
            .value();

        storage.set(key, newHistory);
        all = newHistory;
    }

    function remove(name, kind, state, stateParams) {
        const history = storage.get(key) || [];
        const item = { name, state, kind, stateParams };
        const itemStr = JSON.stringify(item);

        const newHistory = _.filter(history, h => JSON.stringify(h) !== itemStr);

        storage.set(key, newHistory);
        all = newHistory;
    }

    function getAll() {
        return all || [];
    }

    return {
        put,
        remove,
        getAll
    };
}

store.$inject = [
    "localStorageService"
];


export default store;
