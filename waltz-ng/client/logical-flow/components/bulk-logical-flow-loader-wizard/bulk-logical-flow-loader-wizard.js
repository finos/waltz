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
import { toJson } from 'angular';
import { initialiseData } from '../../../common';
import { CORE_API } from "../../../common/services/core-api-utils";

import template from './bulk-logical-flow-loader-wizard.html';


const sharedPreferenceMappingKey = 'logical-flow-upload-mapping';

const bindings = {
};


const initialState = {
    currentStep: 1,
    columnMappings: null,
    loadedMappings: null,
    mappingsComplete: false,
    newFlows: [],
    sourceData: [],
    sourceColumns: [],
    targetColumns: [
        {key: 'source', name: 'Source', required: true, description: 'The source of the flow'},
        {key: 'target', name: 'Target', required: true, description: 'The target of the flow'},
        {key: 'dataType', name: 'Data Type', required: true, description: 'The flow\'s data type'}
    ]
};


function saveMappings(serviceBroker, sourceColumns, targetColumns, mappings) {
    const obj = toJson({
        sourceColumns,
        targetColumns
    });

    return serviceBroker
        .loadViewData(CORE_API.SharedPreferenceStore.generateKeyRoute, [obj], { force: true })
        .then(keyResult => {
            return serviceBroker
                .execute(CORE_API.SharedPreferenceStore.save, [keyResult.data, sharedPreferenceMappingKey, toJson(mappings)])
                .then(saveResult => saveResult.data);
        });
}


function loadMappings(serviceBroker, sourceColumns, targetColumns) {
    const obj = toJson({
        sourceColumns,
        targetColumns
    });

    return serviceBroker
        .loadViewData(CORE_API.SharedPreferenceStore.generateKeyRoute, [obj], { force: true })
        .then(r => {
            return serviceBroker
                .execute(CORE_API.SharedPreferenceStore.getByKeyAndCategory, [r.data, sharedPreferenceMappingKey])
                .then(r => r.data)
                .then(preference => preference ? JSON.parse(preference.value) : undefined);
        });
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.backVisible = () => {
      return vm.currentStep > 1;
    };

    vm.back = () => {
        vm.currentStep--;
        if(vm.currentStep === 3) {
            vm.parseFlows();
        }
    };

    vm.nextVisible = () => {
        return vm.currentStep < 4;
    };

    vm.canGoNext = () => {
        switch (vm.currentStep) {
            case 1:
                return vm.sourceData.length > 0 && vm.sourceColumns.length > 0;
            case 2:
                return !_.isEmpty(vm.columnMappings) && vm.mappingsComplete;
            case 3:
                return vm.newFlows.length > 0;
            default:
                throw 'Unrecognised step number: ' + vm.currentStep;
        }
    };

    vm.next = () => {
        vm.currentStep++;
        if(vm.currentStep === 3) {
            saveMappings(serviceBroker, vm.sourceColumns, vm.targetColumns, vm.columnMappings);
            vm.parseFlows();
        }

        if(vm.currentStep === 4) {
            vm.uploadFlows();
        }
    };

    vm.spreadsheetLoaded = (event) => {
        vm.sourceData = event.rowData;
        vm.sourceColumns = _.map(event.columnDefs, 'name');

        loadMappings(serviceBroker, vm.sourceColumns, vm.targetColumns)
            .then(mappings => {
                vm.loadedMappings = mappings;
            });
    };

    vm.onMappingsChanged = (event) => {
        vm.columnMappings = event.mappings;
        vm.mappingsComplete = event.isComplete();
    };

    vm.parserInitialised = (api) => {
        vm.parseFlows = api.parseFlows;
    };

    vm.parseComplete = (event) => {
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
        }
    };

    vm.uploaderInitialised = (api) => {
        vm.uploadFlows = api.uploadFlows;
    };

    vm.uploadComplete = () => {
    };

}


controller.$inject = [
    'ServiceBroker'
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzBulkLogicalFlowLoaderWizard'
};
