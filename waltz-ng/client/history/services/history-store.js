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
