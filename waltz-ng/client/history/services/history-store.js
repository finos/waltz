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

import _ from 'lodash';

const key = 'history_1';

function store(storage) {

    let all = storage.get(key);

    function put(name, kind, state, stateParams) {
        const history = storage.get(key) || [];
        const item = { name, state, kind, stateParams };

        const newHistory = _.chain([item, ...history])
            .uniq(h => JSON.stringify(h))
            .take(10)
            .value();

        storage.set(key, newHistory);
        all = newHistory;
    }

    return {
        put,
        all
    };
}

store.$inject = ['localStorageService', '$state', '$stateParams'];


export default store;
