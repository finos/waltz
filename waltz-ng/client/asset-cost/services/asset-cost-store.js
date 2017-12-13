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

import {checkIsIdSelector} from "../../common/checks";


export function store($http, root) {

    const BASE = `${root}/asset-cost`;

    const findByCode = code =>
        $http
            .get(`${BASE}/code/${code}`)
            .then(result => result.data);


    const findByAppId = appId =>
        $http
            .get(`${BASE}/app-cost/${appId}`)
            .then(result => result.data);


    const findAppCostsByAppIdSelector = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${BASE}/app-cost/apps`, options)
            .then(result => result.data);
    };


    const findTopAppCostsByAppIdSelector = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${BASE}/app-cost/top-apps`, options)
            .then(result => result.data);
    };


    const findTotalCostForAppSelector = (options, year) => {
        checkIsIdSelector(options);
        var path = `${BASE}/app-cost/apps/total`;
        const params = year
            ? { params: { year } }
            : {};

        return $http
            .post(path, options, params)
            .then(result => result.data);
    };


    const calculateCombinedAmountsForSelector = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${BASE}/amount/app-selector`, options)
            .then(r => r.data);
    };


    return {
        findByCode,
        findByAppId,
        findAppCostsByAppIdSelector,
        findTopAppCostsByAppIdSelector,
        findTotalCostForAppSelector,
        calculateCombinedAmountsForSelector
    };
}

store.$inject = ['$http', 'BaseApiUrl'];



export const serviceName = 'AssetCostStore';


export const AssetCostStore_API = {
    findByCode: {
        serviceName,
        serviceFnName: 'findByCode',
        description: 'executes findByCode'
    },
    findByAppId: {
        serviceName,
        serviceFnName: 'findByAppId',
        description: 'executes findByAppId'
    },
    findAppCostsByAppIdSelector: {
        serviceName,
        serviceFnName: 'findAppCostsByAppIdSelector',
        description: 'executes findAppCostsByAppIdSelector'
    },
    findTopAppCostsByAppIdSelector: {
        serviceName,
        serviceFnName: 'findTopAppCostsByAppIdSelector',
        description: 'executes findTopAppCostsByAppIdSelector'
    },
    findTotalCostForAppSelector: {
        serviceName,
        serviceFnName: 'findTotalCostForAppSelector',
        description: 'executes findTotalCostForAppSelector'
    },
    calculateCombinedAmountsForSelector: {
        serviceName,
        serviceFnName: 'calculateCombinedAmountsForSelector',
        description: 'executes calculateCombinedAmountsForSelector'
    }
};


