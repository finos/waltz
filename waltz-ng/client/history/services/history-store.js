/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";

const key = 'history_2';

function store(storage) {

    let all = storage.get(key);

    function put(name, kind, state, stateParams) {
        const history = storage.get(key) || [];
        const item = { name, state, kind, stateParams };

        const newHistory = _.chain([item, ...history])
            .uniqBy(h => JSON.stringify(h))
            .take(10)
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
    'localStorageService'
];


export default store;
