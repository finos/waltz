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

import angular from 'angular';
import template from './inline-edit-area.html';

const Modes = {
    SAVING: Symbol(),
    VIEWING: Symbol(),
    EDITING: Symbol()
};


function controller() {
    const vm = this;

    vm.mode = Modes.VIEWING;
    vm.Modes = Modes;

    vm.$onChanges = (c) => {
        if(c.value) {
            // go back to viewing mode
            vm.mode = Modes.VIEWING;
        }
    };

    vm.edit = () => {
        vm.editor = {
            modifiableValue: angular.copy(vm.value)
        };
        vm.mode = Modes.EDITING;
    };

    vm.cancel = () => {
        vm.mode = Modes.VIEWING;
    };

    vm.mySave = () => {
        vm.mode = Modes.SAVING;
        const valueToSave = vm.editor.modifiableValue;
        vm.onSave(valueToSave, vm.value)
            .then((msg) => {
                vm.mode = Modes.VIEWING;
                vm.value = vm.editor.modifiableValue;
            }, (msg) => {
                console.log('Error', msg)
            });
    };

    vm.onKeydown = (evt) => {
        if (evt.keyCode === 13 && (evt.metaKey || evt.ctrlKey)) {
            vm.mySave();
        }
    }
}


controller.$inject = [];

const bindings = {
    placeholder: '@',
    value: '<',
    onSave: '<'
};


const component = {
    template,
    controller,
    bindings
};


export default component;
