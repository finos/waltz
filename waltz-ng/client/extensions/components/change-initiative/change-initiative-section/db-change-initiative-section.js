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

import {CORE_API} from '../../../../common/services/core-api-utils';
import {initialiseData} from '../../../../common';

import template from './db-change-initiative-section.html';


const bindings = {
    name: '@',
    parentEntityRef: '<'
};


const initialState = {
    name: 'DB Change Initiatives',
    changeInitiatives: [],
    visibility: {
        sourcesOverlay: false
    }
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        if(vm.parentEntityRef) {
            let promise = null;
            if (vm.parentEntityRef.kind === 'PERSON') {
                promise = serviceBroker
                    .loadViewData(CORE_API.PersonStore.getById, [vm.parentEntityRef.id])
                    .then(person => serviceBroker.loadViewData(
                        CORE_API.InvolvementStore.findChangeInitiativesForEmployeeId,
                        [person.data.employeeId]));
            } else if (vm.parentEntityRef.kind === 'CHANGE_INITIATIVE') {
                promise = serviceBroker.loadViewData(
                    CORE_API.ChangeInitiativeStore.findByParentId,
                    [vm.parentEntityRef.id]);
            } else {
                promise = serviceBroker.loadViewData(
                    CORE_API.ChangeInitiativeStore.findByRef,
                    [vm.parentEntityRef.kind, vm.parentEntityRef.id]);
            }

            promise
                .then(result => {
                    vm.changeInitiatives = result.data;
            });
        }
    };

    vm.onSelect = ci => vm.selectedChangeInitiative = ci;

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
    id: 'waltzDbChangeInitiativeSection'
};
