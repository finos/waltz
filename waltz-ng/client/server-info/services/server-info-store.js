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

function service(http, baseUrl) {

    const BASE = `${baseUrl}/server-info`;

    const findByAssetCode = (assetCode) =>
        http.get(`${BASE}/asset-code/${assetCode}`)
            .then(result => result.data);

    const findByAppId = (appId) =>
        http.get(`${BASE}/app-id/${appId}`)
            .then(result => result.data);


    const findStatsForSelector = (id, kind, scope = 'EXACT') =>
        http.post(`${BASE}/apps/stats`, { scope, entityReference: { id, kind }})
            .then(result => result.data);

    return {
        findByAssetCode,
        findByAppId,
        findStatsForSelector
    };
}

service.$inject = ['$http', 'BaseApiUrl'];


export default service;
