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

import template from './entity-data-sourcing-panel.html';
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";


import {nest} from 'd3-collection';
import {refToString} from "../../../common/entity-utils";


const bindings = {
    parentEntityRef:  '<'
};


const initialState = {
    levels: [],
    levelStatsByKey : {},
};


const combinedDataNester = nest()
    .key(d => refToString(d.logicalFlow.source))
    .key(d => d.dataTypeId)
    .rollup(xs => _.countBy(xs, 'rating'));



/**
 * Returns list looking like:
 * ```
 * [
 *   {
 *      source: {..},
 *      dataTypes: [
 *        {
 *          dataType: {..},
 *          counts : { DISCOURAGED: 10, .. }
*         }
 *      ]
 *    },
 *    ..
 *  ]
 * ```
 * @param level
 * @param flows
 * @param decorators
 */
function combineFlowsAndDecorators(level = 0, flows = [], decorators = []) {
    const flowsById = _.keyBy(flows, 'id');

    return _.map(decorators, d => {
        return {
            level,
            dataTypeId: d.decoratorEntity.id,
            logicalFlow: flowsById[d.dataFlowId],
            rating: d.rating
        };
    });
}



function recalcSources(levels = [], dataTypesById = []) {

    const flattenedLevels = _.flatMap(levels);

    const sourcesByRef = _.chain(flattenedLevels)
        .map('logicalFlow.source')
        .keyBy(refToString)
        .value();

    const nested = combinedDataNester.entries(flattenedLevels);

    return _
        .chain(nested)
        .map(d => {
            return {
                source: sourcesByRef[d.key],
                dataTypes: _
                    .chain(d.values)
                    .map(dt => {
                        return {
                            dataType: dataTypesById[dt.key],
                            counts: dt.value,
                            key: d.key + '/' + dt.key
                        };
                    })
                    .sortBy('dataType.name')
                    .value()
            };
        })
        .sortBy('source.name')
        .value();
}


function ensureDepth(minDepth = 1, existing = []) {
    if (existing.length < minDepth) {
        const missing = minDepth - existing.length;
        const extras = _.map(_.range(missing), x => ensureStatValues({}));
        return _.concat(existing, extras);
    } else {
        return _.concat(existing, []);
    }
}


function ensureStatValues(existing = {}) {
    return {
        DISCOURAGED: existing.DISCOURAGED || 0,
        PRIMARY: existing.PRIMARY || 0,
        SECONDARY: existing.SECONDARY || 0,
        NO_OPINION: existing.NO_OPINION || 0
    };
}

function updateLevelStats(levelStatsByKey = {}, levelNumber = 0, data = []) {

    const nested = combinedDataNester.entries(data);

    _.each(nested, d => {
        _.each(d.values, dt => {
            const key = d.key + '/' + dt.key;
            const keyData = ensureDepth(levelNumber + 1, levelStatsByKey[key]);

            keyData[levelNumber] = ensureStatValues(dt.value);
            levelStatsByKey[key] = keyData;
        });
    });

    return levelStatsByKey;
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.loadNextLevel = (refs = []) => {
        const nextLevelNumber = vm.levels.length;

        if (! refs.length) {
            const previousLevel = vm.levels[nextLevelNumber - 1];
            refs = _
                .chain(previousLevel || [])
                .map(d => d.logicalFlow.source)
                .uniqBy(refToString)
                .value();
        }

        const flowPromise = serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.findUpstreamFlowsForEntityReferences,
                [ refs ])
            .then(r => r.data);

        const levelDataAcc = {
            logicalFlows: [],
            decorators: []
        };

        return flowPromise
            .then(flows => {
                levelDataAcc.logicalFlows = flows;
                return _.map(flows, 'id');
            })
            .then(ids => serviceBroker.loadViewData(
                CORE_API.LogicalFlowDecoratorStore.findByFlowIdsAndKind,
                [ ids, 'DATA_TYPE']))
            .then(r => levelDataAcc.decorators = r.data)
            .then(() => {
                const combined = combineFlowsAndDecorators(
                    nextLevelNumber,
                    levelDataAcc.logicalFlows,
                    levelDataAcc.decorators);
                updateLevelStats(vm.levelStatsByKey, nextLevelNumber, combined);
                vm.levels = _.concat(vm.levels, [combined]);
            })
            .then(() => {
                vm.sources = recalcSources(vm.levels, vm.dataTypesById);
            })
    };

    vm.$onInit = () => {
        const refs = [vm.parentEntityRef];

        serviceBroker.loadAppData(CORE_API.DataTypeStore.findAll, [])
            .then(r => vm.dataTypesById = _.keyBy(r.data, 'id'))
            .then(() => vm.loadNextLevel(refs))
            .then(() => vm.loadNextLevel())
    };

    vm.$onChanges = () => {
    };
}


controller.$inject = [
    'ServiceBroker'
];


export const component = {
    bindings,
    controller,
    template
};


export const id = 'waltzEntityDataSourcingPanel';