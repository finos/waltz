/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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
import { initialiseData } from '../../../common';

import template from './bulk-logical-flow-loader-wizard.html';


const bindings = {
    parentEntityRef: '<',
    scope: '@'
};


const initialState = {
    currentStep: 1,
    columnMappings: {
        from_app_nar: {name: 'source', required: true},
        'Target code': {name: 'target', required: true},
        'Data Types': {name: 'dataType', required: true},
        'Source Pr': {name: 'provenance', required: false},
    },
    sourceData: [],
    sourceColumns: [],
    targetColumns: [
        {name: 'source', required: true},
        {name: 'target', required: true},
        {name: 'dataType', required: true},
        {name: 'provenance', required: false},
    ]
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
    };

    vm.$onChanges = (changes) => {
    };

    vm.back = () => {
        vm.currentStep--;
    };

    vm.next = () => {
        vm.currentStep++;
    };

    vm.spreadsheetLoaded = (event) => {
        console.log('spreadsheet loaded: ', event);
        vm.sourceData = event.rowData;
        vm.sourceColumns = _.map(event.columnDefs, 'name');
    };

    vm.onMappingsChanged = (event) => {
        console.log('mappings changed: ', event.mappings, event.isComplete());
        if(event.isComplete()) {
            vm.columnMappings = event.mappings;
        }
    };

    vm.parseComplete = (event) => {
        console.log('parse complete: ', event.data, event.isComplete());
        if(event.isComplete()) {
            vm.newFlows = _
                .chain(event.data)
                .filter(f => f.existing === null)
                .map(f => ({
                    source: f.source.entityRef,
                    target: f.target.entityRef,
                    dataType: f.dataType.entityRef,
                    provenance: f.provenance
                }))
                .value();
            console.log('new flows: ', vm.newFlows);
        }
    };

    vm.uploadComplete = (event) => {
        console.log('upload complete: ', event);
    };

}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzBulkLogicalFlowLoaderWizard'
};
