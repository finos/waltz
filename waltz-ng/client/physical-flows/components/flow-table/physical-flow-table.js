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

import {initialiseData, invokeFunction, termSearch} from "../../../common";
import {mkEntityLinkGridCell, mkLinkGridCell} from "../../../common/grid-utils";
import template from './physical-flow-table.html';

const bindings = {
    lineage: '<',
    onInitialise: '<'
};


const initialState = {
    filteredLineage: []
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        vm.filteredLineage = vm.lineage;
    };


    const fields = [
        'targetEntity.name',
        'flow.description',
        'flow.basisOffset',
        'flow.transport',
        'flow.frequency',
        'specification.name',
        'specification.externalId',
        'specification.format',
        'specification.description',
        'specification.owningEntity.name',
    ];


    vm.filterLineage = query => {
        vm.filteredLineage = termSearch(vm.lineage, query, fields);
    };


    vm.columnDefs = [
        mkLinkGridCell('Name', 'specification.name', 'flow.id', 'main.physical-flow.view'),
        mkEntityLinkGridCell('Source', 'sourceEntity', 'left'),
        mkEntityLinkGridCell('Target', 'targetEntity', 'left'),
        {
            field: 'specification.format',
            name: 'Format',
            cellFilter: 'toDisplayName:"dataFormatKind"'
        }
    ];


    vm.onGridInitialise = (api) => {
        vm.gridApi = api;
    };


    vm.exportGrid = () => {
        vm.gridApi.exportFn('lineage-reports.csv');
    };

    invokeFunction(vm.onInitialise, {export: vm.exportGrid });

}


const component = {
    bindings,
    template,
    controller
};


export default component;