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
import {initialiseData} from "../../../common";
import {mkEntityLinkGridCell, mkLinkGridCell} from "../../../common/grid-utils";
import template from './physical-data-section.html';


const bindings = {
    primaryRef: '<',
    physicalFlows: '<',
    specifications: '<',
    logicalFlows: '<',
    onInitialise: '<'
};


const initialState = {
    primaryRef: null,  // entityReference
    physicalFlows: [],
    logicalFlows: [],
    specifications: [],
    onInitialise: (e) => {}
};


/**
 * Given an enriched flow returns a boolean indicating
 * if any enriched fields are missing
 * @param f
 */
const isIncomplete = f => !f.logicalFlow ||  !f.specification;


/**
 * Takes a entityReference and returns a new function which takes an
 * enriched flow and returns true if the entity is consuming the flow
 * or false if it is producing the flow
 * @param entityRef
 * @private
 */
const _isConsumer = (entityRef) => (f) => {
    const target = f.logicalFlow.target;
    return target.id === entityRef.id && target.kind === entityRef.kind;
};


function mkData(primaryRef,
                specifications = { produces: [], consumes: [] },
                physicalFlows = [],
                logicalFlows = [])
{
    if (!primaryRef) return [];

    const specsById = _.keyBy(specifications, 'id');
    const logicalById = _.keyBy(logicalFlows, 'id');

    const enrichFlow = (pf) => {
        return {
            physicalFlow: pf,
            logicalFlow: logicalById[pf.logicalFlowId],
            specification: specsById[pf.specificationId]
        };
    };

    const [consumes, produces] = _
        .chain(physicalFlows)
        .map(enrichFlow)
        .reject(isIncomplete)
        .partition(_isConsumer(primaryRef))
        .value();

    return { consumes, produces };
}


function controller() {

    const vm = initialiseData(this, initialState);

    vm.produceColumnDefs = [
        Object.assign(mkLinkGridCell('Name', 'specification.name', 'physicalFlow.id', 'main.physical-flow.view'), { width: "20%"} ),
        { field: 'specification.externalId', displayName: 'Ext. Id', width: "10%" },
        Object.assign(mkEntityLinkGridCell('Receiver(s)', 'logicalFlow.target', 'left'), { width: "15%" }),
        { field: 'specification.format', displayName: 'Format', width: "8%", cellFilter: 'toDisplayName:"dataFormatKind"' },
        { field: 'physicalFlow.transport', displayName: 'Transport', width: "14%", cellFilter: 'toDisplayName:"transportKind"' },
        { field: 'physicalFlow.frequency', displayName: 'Frequency', width: "10%", cellFilter: 'toDisplayName:"frequencyKind"' },
        { field: 'physicalFlow.criticality', displayName: 'Criticality', width: "10%", cellFilter: 'toDisplayName:"physicalFlowCriticality"' },
        { field: 'specification.description', displayName: 'Description', width: "23%" }
    ];

    vm.consumeColumnDefs = [
        Object.assign(mkLinkGridCell('Name', 'specification.name', 'physicalFlow.id', 'main.physical-flow.view'), { width: "20%"} ),
        { field: 'specification.externalId', displayName: 'Ext. Id', width: "10%" },
        Object.assign(mkEntityLinkGridCell('Source', 'logicalFlow.source', 'left'), { width: "15%"} ),
        { field: 'specification.format', displayName: 'Format', width: "8%", cellFilter: 'toDisplayName:"dataFormatKind"' },
        { field: 'physicalFlow.transport', displayName: 'Transport', width: "14%", cellFilter: 'toDisplayName:"transportKind"' },
        { field: 'physicalFlow.frequency', displayName: 'Frequency', width: "10%", cellFilter: 'toDisplayName:"frequencyKind"' },
        { field: 'physicalFlow.criticality', displayName: 'Criticality', width: "10%", cellFilter: 'toDisplayName:"physicalFlowCriticality"' },
        { field: 'specification.description', displayName: 'Description', width: "23%" }
    ];

    vm.unusedSpecificationsColumnDefs = [
        { field: 'name', displayName: 'Name' },
        { field: 'format', displayName: 'Format', cellFilter: 'toDisplayName:"dataFormatKind"' },
        { field: 'description', displayName: 'Description' }
    ];

    vm.$onChanges = () => {
        Object.assign(vm, mkData(vm.primaryRef, vm.specifications, vm.physicalFlows, vm.logicalFlows));
    };

    vm.onProducesGridInitialise = (e) => {
        vm.producesExportFn = e.exportFn;
    };

    vm.onConsumesGridInitialise = (e) => {
        vm.consumesExportFn = e.exportFn;
    };

    vm.onUnusedSpecificationsGridInitialise = (e) => {
        vm.unusedSpecificationsExportFn = e.exportFn;
    };

    vm.exportProduces = () => {
        vm.producesExportFn('produces.csv');
    };

    vm.exportConsumes = () => {
        vm.consumesExportFn('consumes.csv');
    };

    vm.exportUnusedSpecifications = () => {
        vm.unusedSpecificationsExportFn('unused-specifications.csv');
    };

    // callback
    vm.onInitialise({
        exportProducesFn: vm.exportProduces,
        exportConsumesFn: vm.exportConsumes,
        exportUnusedSpecificationsFn: vm.exportUnusedSpecifications
    });
}

controller.$inject = ['$scope'];


const component = {
    template,
    bindings,
    controller
};


export default component;