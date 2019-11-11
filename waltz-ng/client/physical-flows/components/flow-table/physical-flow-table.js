/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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
import {initialiseData, invokeFunction} from "../../../common";
import {mkEnumGridCell, mkLinkGridCell} from "../../../common/grid-utils";
import {CORE_API} from "../../../common/services/core-api-utils";

import template from "./physical-flow-table.html";

const bindings = {
    parentEntityRef: '<',
    onInitialise: '<',
    optionalColumnDefs: '<'
};


const initialState = {
    tableData: []
};


function mkData(primaryRef,
                specifications = [],
                physicalFlows = [],
                logicalFLows = [])
{
    if (!primaryRef) return [];

    const specsById = _.keyBy(specifications, 'id');
    const logicalFlowsById = _.keyBy(logicalFLows, 'id');

    const enrichFlow = (pf) => {
        return {
            physicalFlow: pf,
            specification: specsById[pf.specificationId],
            logicalFlow: logicalFlowsById[pf.logicalFlowId]
        };
    };

    return _.map(physicalFlows, enrichFlow);
}


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);
    const defaultColumnDefs = [
        Object.assign(mkLinkGridCell('Name', 'specification.name', 'physicalFlow.id', 'main.physical-flow.view'), { width: "20%"} ),
        { field: 'specification.externalId', displayName: 'Ext. Id'},
        Object.assign(mkEnumGridCell("Observation", "physicalFlow.freshnessIndicator", "FreshnessIndicator", true, true), { width: "10%"}),
        { field: 'specification.format', displayName: 'Format', cellFilter: 'toDisplayName:"dataFormatKind"' },
        { field: 'physicalFlow.transport', displayName: 'Transport', cellFilter: 'toDisplayName:"TransportKind"' },
        { field: 'physicalFlow.frequency', displayName: 'Frequency', cellFilter: 'toDisplayName:"frequencyKind"' },
        { field: 'physicalFlow.criticality', displayName: 'Criticality', cellFilter: 'toDisplayName:"physicalFlowCriticality"' },
        { field: 'specification.description', displayName: 'Description', }
    ];

    vm.columnDefs = vm.optionalColumnDefs == null ? defaultColumnDefs : vm.optionalColumnDefs;

    vm.$onInit = () => {
        vm.selector = {
            entityReference: vm.parentEntityRef,
            scope: 'EXACT'
        };

        const flowPromise = serviceBroker
            .loadViewData(CORE_API.PhysicalFlowStore.findBySelector, [vm.selector])
            .then(r => r.data);
        const specPromise = serviceBroker
            .loadViewData(CORE_API.PhysicalSpecificationStore.findBySelector, [vm.selector])
            .then(r => r.data);

        loadTableData(flowPromise, specPromise);

    };

    function loadTableData(flowPromise, specPromise) {
        if (vm.parentEntityRef.kind === "TAG") {
            const logicalFlowPromise = serviceBroker
                .loadViewData(CORE_API.LogicalFlowStore.findBySelector, [vm.selector])
                .then(r => r.data);

            $q.all([flowPromise, specPromise, logicalFlowPromise])
                .then(([physicalFlows, specs, logicalFlows]) => {
                    vm.tableData = mkData(vm.parentEntityRef, specs, physicalFlows, logicalFlows);
                });
        } else {
            $q.all([flowPromise, specPromise])
                .then(([physicalFlows, specs]) => {
                    vm.tableData = mkData(vm.parentEntityRef, specs, physicalFlows);
                });
        }
    }

    vm.$onChanges = (changes) => {
    };

    vm.onGridInitialise = (api) => {
        vm.gridApi = api;
    };

    vm.exportGrid = () => {
        vm.gridApi.exportFn('physical_flows.csv');
    };

    invokeFunction(vm.onInitialise, {export: vm.exportGrid });
}


controller.$inject = [
    '$q',
    'ServiceBroker',
];


const component = {
    bindings,
    template,
    controller
};


export default component;