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
import _ from "lodash";
import {event} from "d3-selection";
import {initialiseData} from "../../../common";
import {downloadTextFile} from "../../../common/file-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkTweakers} from "../source-and-target-graph/source-and-target-utilities";

import template from "./source-and-target-panel.html";


const bindings = {
    entityRef: '<',
    logicalFlows: '<',
    decorators: '<',
    physicalFlows: '<',
    physicalSpecifications: '<'
};


const initialState = {
    filteredFlowData: {
        selectedTypeId: 0,
        flows: [],
        decorators: []
    }
};


function calcPhysicalFlows(physicalFlows = [], specifications = [], logicalFlowId) {
    const specsById = _.keyBy(specifications, 'id');

    return _.chain(physicalFlows)
        .filter(pf => pf.logicalFlowId === logicalFlowId)
        .map(pf => Object.assign({}, pf, { specification: specsById[pf.specificationId] }))
        .value();
}


function filterByType(typeId, flows = [], decorators = []) {
    if (typeId == 0) {
        return {
            selectedTypeId: 0,
            decorators,
            flows
        };
    }

    const ds = _.filter(decorators, d => d.decoratorEntity.id === typeId);
    const dataFlowIds = _.map(ds, "dataFlowId");
    const fs = _.filter(flows, f => _.includes(dataFlowIds, f.id));

    return {
        selectedTypeId: typeId,
        decorators: ds,
        flows: fs
    };
}


// flowId -> [ { id (typeId), rating }... ]
function mkTypeInfo(decorators = []) {
    return _.chain(decorators)
        .filter({ decoratorEntity: { kind: 'DATA_TYPE' }})
        .groupBy(d => d.dataFlowId)
        .mapValues(xs => _.map(xs, x => {
            return {
                id: x.decoratorEntity.id,
                rating: x.rating
            };
        }))
        .value();
}


function calculateSourceAndTargetFlowsByEntity(primaryEntity, logical = []) {
    if (! primaryEntity) return {};


    const sourceFlowsByEntityId = _.chain(logical)
        .filter(f => f.target.id === primaryEntity.id && f.target.kind === primaryEntity.kind)
        .reduce((acc, f) => { acc[f.source.id] = f.id; return acc; }, {})
        .value();

    const targetFlowsByEntityId = _.chain(logical)
        .filter(f => f.source.id === primaryEntity.id && f.source.kind === primaryEntity.kind)
        .reduce((acc, f) => { acc[f.target.id] = f.id; return acc; }, {})
        .value();

    return {
        sourceFlowsByEntityId,
        targetFlowsByEntityId
    };
}


function scrollIntoView(element, $window) {
    element.scrollIntoView({
        behavior: "smooth",
        block: "start",
    });
    $window.scrollBy(0, -90);
}


function controller($element, $timeout, $window, displayNameService, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.showAll = () => vm.filteredFlowData = filterByType(0, vm.logicalFlows, vm.decorators);
    vm.$onChanges = (changes) => {

        if (changes.logicalFlows || changes.decorators) vm.filteredFlowData = vm.showAll();

        const keyedLogicalFlows = calculateSourceAndTargetFlowsByEntity(
            vm.entityRef,
            vm.logicalFlows);

        const logicalFlowsById = _.keyBy(vm.logicalFlows, 'id');

        function select(entity, type, logicalFlowId, evt) {
            const typeInfoByFlowId = mkTypeInfo(vm.decorators);
            const types = typeInfoByFlowId[logicalFlowId] || [];
            const logicalFlow = logicalFlowsById[logicalFlowId];
            return {
                type,
                types,
                physicalFlows: calcPhysicalFlows(
                    vm.physicalFlows,
                    vm.physicalSpecifications,
                    logicalFlowId),
                entity,
                logicalFlowId,
                logicalFlow,
                y: evt.layerY
            };
        }

        const baseTweakers = {
            source: {
                onSelect: (entity, evt) => $timeout(() => {
                    const flowId = keyedLogicalFlows.sourceFlowsByEntityId[entity.id];
                    vm.selected = select(entity, 'source', flowId, evt);
                })
            },
            target: {
                onSelect: (entity, evt) => $timeout(() => {
                    const flowId = keyedLogicalFlows.targetFlowsByEntityId[entity.id];
                    vm.selected = select(entity, 'target', flowId, evt);
                })
            },
            type: {
                onSelect: d => {
                    event.stopPropagation();
                    $timeout(() =>  {
                        return vm.filteredFlowData = filterByType(
                            d.id,
                            vm.logicalFlows,
                            vm.decorators)
                    }).then(() => {
                        scrollIntoView($element[0], $window);
                    });

                }
            },
            typeBlock: {
                onSelect: () => {
                    event.stopPropagation();
                    $timeout(() => {
                        if (vm.filteredFlowData.selectedTypeId > 0) {
                            vm.showAll();
                        }
                    });
                }
            }
        };

        vm.tweakers = mkTweakers(
            baseTweakers,
            vm.physicalFlows,
            vm.logicalFlows);
    };


    vm.exportLogicalFlowData = () => {
        const header = [
            "Source",
            "Source code",
            "Target",
            "Target code",
            "Data Types"
        ];

        const dataTypesByFlowId = _
            .chain(vm.decorators)
            .filter(d => d.decoratorEntity.kind === 'DATA_TYPE')
            .map(d => ( { id: d.dataFlowId, code: d.decoratorEntity.id }))
            .groupBy('id')
            .value();

        const calcDataTypes = (fId) => {
            const dts = dataTypesByFlowId[fId] || [];

            return _.chain(dts)
                .map(dt => dt.code)
                .map(code => displayNameService.lookup('dataType', code))
                .value()
                .join('; ');
        };

        const appIds = _
            .chain(vm.logicalFlows)
            .flatMap(f => ([ f.source, f.target ]))
            .filter(r => r.kind === 'APPLICATION')
            .map(r => r.id)
            .uniq()
            .value();

        serviceBroker
            .loadViewData(CORE_API.ApplicationStore.findByIds, [appIds])
            .then(r => {
                const appsById = _.keyBy(r.data, 'id');

                const resolveCode = (ref) => {
                    const pathToNameAttr = [ref.id, 'assetCode'];
                    return ref.kind === 'APPLICATION'
                        ? _.get(appsById, pathToNameAttr, '-')
                        : ref.kind;
                };

                const dataRows = _.map(vm.logicalFlows, f => {
                    return [
                        f.source.name,
                        resolveCode(f.source),
                        f.target.name,
                        resolveCode(f.target),
                        calcDataTypes(f.id)
                    ]
                });

                const rows = _.concat(
                    [header],
                    dataRows);

                downloadTextFile(rows, ',', 'logical_flows.csv');
            })
    };
}


controller.$inject = [
    '$element',
    '$timeout',
    '$window',
    'DisplayNameService',
    'ServiceBroker'
];


const component = {
    template,
    bindings,
    controller
};


const id = "waltzSourceAndTargetPanel";


export default {
    component,
    id
};