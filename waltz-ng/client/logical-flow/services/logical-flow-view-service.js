/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from 'lodash';
import {notEmpty} from "../../common";


const initData = {
    loadingStats: false,
    loadingFlows: false,
    decorators: [],
    flows: [],
    options: {},
    stats: {}
};


function service($q,
                 dataTypeUsageStore,
                 logicalFlowDecoratorStore,
                 logicalFlowStore)
{
    let data = initData;

    function initialise(id, kind, scope = 'CHILDREN') {
        reset();
        data.loadingStats = true;

        data.options = _.isObject(id)
            ? id
            : { entityReference: { id, kind }, scope };

        const statStore = data.options.entityReference.kind === 'DATA_TYPE'
            ? dataTypeUsageStore
            : logicalFlowStore;

        return statStore
            .calculateStats(data.options)
            .then(stats => {
                data.loadingStats = false;
                data.stats = stats;
                return data;
            });
    }


    function loadDetail() {
        if (notEmpty(data.flows)) {
            return $q.when(data);
        }

        data.loadingFlows = true;

        const flowPromise = logicalFlowStore
            .findBySelector(data.options)
            .then(flows => data.flows = flows);

        const decoratorPromise = logicalFlowDecoratorStore
            .findBySelector(data.options)
            .then(decorators => data.decorators = decorators);

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
    'DataTypeUsageStore',
    'LogicalFlowDecoratorStore',
    'LogicalFlowStore'
];


export default service;