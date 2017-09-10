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

import _ from 'lodash';
import {notEmpty} from "../../common";
import {CORE_API} from "../../common/services/core-api-utils";


const initData = {
    loadingStats: false,
    loadingFlows: false,
    decorators: [],
    flows: [],
    options: {},
    stats: {}
};


function service($q,
                 serviceBroker)
{
    let data = initData;

    function initialise(id, kind, scope = 'CHILDREN') {
        reset();
        data.loadingStats = true;

        data.options = _.isObject(id)
            ? id
            : { entityReference: { id, kind }, scope };

        const statMethod = data.options.entityReference.kind === 'DATA_TYPE'
            ? CORE_API.DataTypeUsageStore.calculateStats
            : CORE_API.LogicalFlowStore.calculateStats;

        return serviceBroker
            .loadViewData(statMethod, [ data.options ])
            .then(r => {
                data.loadingStats = false;
                data.stats = r.data;
                return data;
            });
    }


    function loadDetail() {
        if (notEmpty(data.flows)) {
            return $q.when(data);
        }

        data.loadingFlows = true;

        const flowPromise = serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.findBySelector,
                [ data.options ])
            .then(r => data.flows = r.data);

        const decoratorPromise = serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowDecoratorStore.findBySelector,
                [ data.options ])
            .then(r => data.decorators = r.data);

        return $q
            .all([flowPromise, decoratorPromise])
            .then(() => data.loadingFlows = false)
            .then(() => ({...data}));
    }


    function reset() {
        data = { ...initData };
    }


    return {
        initialise,
        loadDetail,
        reset
    };
}


service.$inject = [
    '$q',
    'ServiceBroker'
];


export default service;