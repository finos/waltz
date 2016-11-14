
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


function accessLogStore($http, BaseApiUrl) {
    const BASE = `${BaseApiUrl}/access-log`;

    const write = (state, params) => {
        const paramStr = _.isObject(params)
            ? JSON.stringify(params)
            : params;

        return $http
            .post(`${BASE}/${state}/${paramStr}`, null)
            .then(r => r.data);
    };


    const findForUserName = (userName) => $http
        .get(`${BASE}/user/${userName}`)
        .then(r => r.data);


    return {
        findForUserName,
        write
    };
}


accessLogStore.$inject = [
    '$http',
    'BaseApiUrl'
];


export default accessLogStore;
