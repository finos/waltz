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
import _ from "lodash";
import {initialiseData, invokeFunction} from "../../../common";
import template from "./physical-spec-definition-panel.html";


const bindings = {
    defaultActiveTabIndex: '<',
    logicalDataElements: '<',
    specDefinition: '<',
    selectableDefinitions: '<',
    onDefinitionSelect: '<',
    onUpdateFieldDescription: '<',
    onUpdateLogicalDataElement: '<'
};


const initialState = {
    logicalDataElementsById: {},
    specDefinition: {},
    selectableDefinitions: [],
    onDefinitionSelect: (def) => console.log('psdp::onDefinitionSelect', def),
    onUpdateFieldDescription: (change, field) => console.log('psdp::onUpdateFieldDescription', { field, change }),
    onUpdateLogicalDataElement: (change, field) => console.log('psdp::onUpdateLogicalDataElement', { field, change })
};


function controller($q) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        if(vm.logicalDataElements) {
            vm.logicalDataElementsById = _.keyBy(vm.logicalDataElements, 'id');
        }
    };

    vm.definitionSelected = (def) => invokeFunction(vm.onDefinitionSelect, def);

    vm.updateDescription = (change, f) => {
        if (_.isEmpty(change.newVal)) return $q.reject("Too short");
        return invokeFunction(vm.onUpdateFieldDescription, change, f);
    };

    vm.updateLogicalElement = (change, f) => {
        return invokeFunction(vm.onUpdateLogicalDataElement, change, f);
    };
}


controller.$inject = [
    '$q'
];


const component = {
    controller,
    template,
    bindings,
    transclude: {
        noDataMessage: 'noDataMessage'
    }
};


export default component;
