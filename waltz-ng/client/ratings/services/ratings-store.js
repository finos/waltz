

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

function store($http, BaseApiUrl) {
    const base = BaseApiUrl + '/capability-rating';

    function findByParent(kind, id) {
        console.log("DEPRECATED: use app-capability-store instead");
        return $http
            .get(`${base}/parent/${kind}/${id}`)
            .then(result => result.data);
    }


    function update(action) {
        console.log("DEPRECATED: use app-capability-store instead");
        return $http
            .post(`${base}`, action);
    }


    function findByAppIds(ids) {
        console.log("DEPRECATED: use rating-store:findByAppIdSelector instead");
        return $http
            .post(`${base}/apps`, ids)
            .then(result => result.data);
    }


    function findByAppIdSelector(selector) {
        console.log("DEPRECATED: use app-capability-store instead");
        return $http
            .post(`${base}/app-selector`, selector)
            .then(result => result.data);
    }


    return {
        findByParent,
        findByAppIds,
        findByAppIdSelector,
        update
    };
}

store.$inject = ['$http', 'BaseApiUrl'];

export default store;
