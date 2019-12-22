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


