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
import {initialiseData, invokeFunction} from "../../../common";
import {mkLinkGridCell} from "../../../common/grid-utils";
import {CORE_API} from "../../../common/services/core-api-utils";

import template from "./physical-flow-table.html";

const bindings = {
    parentEntityRef: '<',
    onInitialise: '<'
};


const initialState = {
    columnDefs: [],
    tableData: [],
};


function mkData(primaryRef,
                specifications = [],
                physicalFlows = [])
{
    if (!primaryRef) return [];

    const specsById = _.keyBy(specifications, 'id');

    const enrichFlow = (pf) => {
        return {
            physicalFlow: pf,
            specification: specsById[pf.specificationId]
        };
    };

    return _.map(physicalFlows, enrichFlow);
}


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.columnDefs = [
        Object.assign(mkLinkGridCell('Name', 'specification.name', 'physicalFlow.id', 'main.physical-flow.view'), { width: "20%"} ),
        { field: 'specification.externalId', displayName: 'Ext. Id', width: "10%" },
        { field: 'specification.format', displayName: 'Format', width: "8%", cellFilter: 'toDisplayName:"dataFormatKind"' },
        { field: 'physicalFlow.transport', displayName: 'Transport', width: "14%", cellFilter: 'toDisplayName:"transportKind"' },
        { field: 'physicalFlow.frequency', displayName: 'Frequency', width: "10%", cellFilter: 'toDisplayName:"frequencyKind"' },
        { field: 'physicalFlow.criticality', displayName: 'Criticality', width: "10%", cellFilter: 'toDisplayName:"physicalFlowCriticality"' },
        { field: 'specification.description', displayName: 'Description', width: "23%" }
    ];

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

        $q.all([flowPromise, specPromise])
            .then(([flows, specs]) => {
                console.log('gt: ', {flows, specs});
                vm.tableData = mkData(vm.parentEntityRef, specs, flows);
                console.log('table data: ', vm.tableData);
            });
    };

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