/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

import template from './drill-grid-definition-selector.html';
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from 'lodash';


const bindings = {
    selectedDefinition: '<',
    onSelectDefinition: '<'
};


const initialState = {
    visibility: {
        manualPicker: false,
        choicePicker: false,
        summary: true,
        loading: true
    },
    definitions: [],
    messages: []
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const calculateMessages = () => {
        if (vm.visibility.loading) return [];
        vm.messages = [];
        if (!vm.selectedDefinition) {
            vm.messages.push('No selected definition');
        }
        if (vm.definitions.length == 0) {
            vm.messages.push('No prebuilt definitions, select the dimensions manually');
        }
    };

    vm.$onInit = () => {
        vm.visibility.loading = true;
        serviceBroker
            .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
            .then(r => {
                vm.axisOptions = _.union(
                    r.data,
                    [ { id: -1, kind: 'DATA_TYPE', name: 'Data Type' } ]);
            });

        serviceBroker
            .loadAppData(CORE_API.DrillGridDefinitionStore.findAll)
            .then(r => {
                vm.definitions = r.data;
                vm.visibility.manualPicker = r.data.length === 0;
                calculateMessages();
                vm.visibility.loading = false;
            });


        if (vm.selectedDefinition) {
            vm.visibility.manualPicker = vm.selectedDefinition.id === null;
        }
    };

    vm.$onChanges = () => calculateMessages();

    vm.onAxisChange = () => {

        const defn = Object.assign(
            {},
            vm.selectedDefinition,
            {
                name: `${vm.selectedDefinition.xAxis.name} / ${vm.selectedDefinition.yAxis.name}`,
                description: 'Custom dimensions'
            });
        vm.onSelectDefinition(defn);
    };

    vm.onOptionSelect = (d) => {
        vm.onSelectDefinition(d);
        vm.switchToSummary();
    };

    vm.switchToSummary = () => {
        const v = vm.visibility;
        v.manualPicker = false;
        v.choicePicker = false;
        v.summary = true;
    };

    vm.switchToChoicePicker = () => {
        const v = vm.visibility;
        v.manualPicker = false;
        v.choicePicker = true;
        v.summary = false;
    };

    vm.switchToManualPicker = () => {
        const v = vm.visibility;
        v.manualPicker = true;
        v.choicePicker = false;
        v.summary = false;
    };
}

controller.$inject = [
    'ServiceBroker'
];


const component = {
    template,
    controller,
    bindings
};

const id = 'waltzDrillGridDefinitionSelector';

export default {
    component,
    id
};
