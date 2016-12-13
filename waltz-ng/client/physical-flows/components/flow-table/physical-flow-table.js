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

import {initialiseData, mkEntityLinkGridCell, mkLinkGridCell, termSearch, invokeFunction} from "../../../common";

const bindings = {
    lineage: '<',
    onInitialise: '<'
};


const template = require('./physical-flow-table.html');


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