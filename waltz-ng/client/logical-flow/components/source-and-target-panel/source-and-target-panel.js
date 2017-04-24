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
import _ from "lodash";
import {event} from "d3-selection";
import {initialiseData} from "../../../common";
import {mkTweakers} from '../source-and-target-graph/source-and-target-utilities';


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


const template = require('./source-and-target-panel.html');


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


function controller($element, $timeout, $window) {
    const vm = initialiseData(this, initialState);

    vm.showAll = () => vm.filteredFlowData = filterByType(0, vm.logicalFlows, vm.decorators);
    vm.$onChanges = (changes) => {

        if (changes.logicalFlows || changes.decorators) vm.filteredFlowData = vm.showAll();

        const keyedLogicalFlows = calculateSourceAndTargetFlowsByEntity(
            vm.entityRef,
            vm.logicalFlows);

        function select(entity, type, logicalFlowId, evt) {
            const typeInfoByFlowId = mkTypeInfo(vm.decorators);
            const types = typeInfoByFlowId[logicalFlowId] || [];
            return {
                type,
                types,
                physicalFlows: calcPhysicalFlows(
                    vm.physicalFlows,
                    vm.physicalSpecifications,
                    logicalFlowId),
                entity,
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
}


controller.$inject = [
    '$element',
    '$timeout',
    '$window'
];


const component = {
    template,
    bindings,
    controller
};


export default component;
