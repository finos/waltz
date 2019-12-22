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

import _ from 'lodash';
import { initialiseData } from '../../common';
import { invokeFunction } from '../../common/index';

import template from './column-mapper.html';

const bindings = {
    sourceColumns: '<',
    targetColumns: '<',
    existingMappings: '<',
    onChange: '<'
};


const initialState = {
    sourceColumns: [],
    targetColumns: [],
    availableSourceColumns: [],
    mappings: {},
    onChange: (event) => console.log('default onChange handler for column-mapper: ', event)
};


function controller() {
    const vm = initialiseData(this, initialState);

    const mkAvailableSourceColumns = () => {
        const mappedSources = _.values(vm.mappings);
        return _.difference(vm.sourceColumns, mappedSources);
    };

    const isComplete = () => {
        const requiredTargets = _
            .chain(vm.targetColumns)
            .filter(tc => tc.required)
            .map(tc => tc.key);
        const mappedSources = _.values(vm.mappings);
        return _.isEmpty(_.difference(requiredTargets, mappedSources));
    };

    vm.$onInit = () => {
        vm.availableSourceColumns = vm.sourceColumns;
    };

    vm.$onChanges = (changes) => {
        if(changes.existingMappings) {
            vm.mappings = vm.existingMappings || {};
            vm.onMappingSelect();
        }
    };

    vm.onMappingSelect = () => {
        vm.availableSourceColumns = mkAvailableSourceColumns();
        const event = {
            mappings: _.omitBy(vm.mappings, (v,k) => _.isEmpty(v)),
            isComplete
        };
        invokeFunction(vm.onChange, event);
    };

    vm.resetMappings = () => {
        vm.mappings = {};
        vm.onMappingSelect();
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
    id: 'waltzColumnMapper'
};
