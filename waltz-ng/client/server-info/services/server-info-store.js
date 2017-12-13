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
import _ from 'lodash';


export function store($http, baseUrl) {

    const BASE = `${baseUrl}/server-info`;

    const findByAssetCode = (assetCode) =>
        $http
            .then(result => result.data);

    const findByAppId = (appId) =>
        $http.get(`${BASE}/app-id/${appId}`)
            .then(result => result.data);


    const findStatsForSelector = (id, kind, scope = 'EXACT') => {
        const options = _.isObject(id)
            ? id
            : {scope, entityReference: {id, kind}};
        return $http
            .post(`${BASE}/apps/stats`, options)
            .then(result => result.data);
    };

    return {
        findByAssetCode,
        findByAppId,
        findStatsForSelector
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = 'ServerInfoStore';


export const ServerInfoStore_API = {
    findByAssetCode: {
        serviceName,
        serviceFnName: 'findByAssetCode',
        description: 'executes findByAssetCodes'
    },
    findByAppId: {
        serviceName,
        serviceFnName: 'findByAppId',
        description: 'executes findByAppId'
    },
    findStatsForSelector: {
        serviceName,
        serviceFnName: 'findStatsForSelector',
        description: 'executes findStatsForSelector'
    }
};