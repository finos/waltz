
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
    const BASE = `${BaseApiUrl}/rated-data-flows`;

    const findByOrgUnitTree = (id) => {
        console.log('rdfs - findByOrgUnitTree: ', id);
        return $http.get(`${BASE}/org-unit-tree/${id}`)
            .then(result => result.data);
    }

    return {
        findByOrgUnitTree
    };
}

service.$inject = [
    '$http',
    'BaseApiUrl'
];

export default service;
