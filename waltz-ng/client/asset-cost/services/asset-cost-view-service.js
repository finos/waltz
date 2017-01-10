/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import {checkIsApplicationIdSelector} from '../../common/checks';
import {notEmpty} from '../../common'

const initData = {
    costs: [],
    appIds: [],
    summary: []
};

function service($q,
                 assetCostStore)
{

    let data = initData;

    function initialise(selector) {
        checkIsApplicationIdSelector(selector);

        data = { ...initData };
        data.options = selector;

        const topCostsPromise = assetCostStore
            .findTopAppCostsByAppIdSelector(selector);
        const totalCostPromise = assetCostStore
            .findTotalCostForAppSelector(selector);

        return $q
            .all([topCostsPromise, totalCostPromise])
            .then(([topCosts = [], total]) => {
                data.summary = topCosts;
                data.total = total;
                return Object.assign({}, data);
            });
    }


    function loadDetail() {
        if (notEmpty(data.costs)) {
            return $q.when(data);
        }

        return assetCostStore
            .findAppCostsByAppIdSelector(data.options)
            .then(costs => {
                data.costs = costs;
                return Object.assign({}, data);
            });
    }



    return {
        initialise,
        loadDetail,
    };
}

service.$inject = [
    '$q',
    'AssetCostStore'
];


export default service;