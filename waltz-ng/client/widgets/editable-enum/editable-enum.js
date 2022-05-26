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

import {initialiseData} from "../../common/index";
import {toOptions} from "../../common/services/enums";

import template from "./editable-enum.html";


const bindings = {
    initialVal: "<",
    enumKind: "@",
    onSave: "<",
    editRole: "@",
    ctx: "<?",
    readOnly: "<?"
};


const initialState = {
    currentVal: null,
    readOnly: false,
    visibility: {
        editor: false
    }
};


function controller($element,
                    $timeout,
                    displayNameService,
                    enumValueService) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        vm.currentVal = vm.initialVal;

        if (vm.enumKind) {
            enumValueService
                .loadEnums()
                .then(d => vm.enumValues = toOptions( d[vm.enumKind]));
        }
    };

    vm.onEdit = () => {
        vm.visibility.editor = true;
        $timeout(() => {
            const input = $element.find("select")[0];
            input.focus();
        });

    };

    vm.doCancel = () => {
        vm.visibility.editor = false;
        vm.saving = false;
        vm.currentVal = vm.initialVal;
    };

    vm.doSave = () => {
        vm.saving = true;

        return vm
            .onSave(vm.currentVal, vm.ctx)
            .then(() => {
                vm.visibility.editor = false;
                vm.saving = false;
            });
    };

    vm.isDirty = () => {
        return vm.currentVal !== null && vm.currentVal !== vm.initialVal;
    };

    vm.onKeyDown = (event) => {
        if (event.ctrlKey && event.keyCode === 13) {  // ctrl + enter
            vm.doSave();
        }
    };

}

controller.$inject = [
    "$element",
    "$timeout",
    "DisplayNameService",
    "EnumValueService"
];


const component = {
    template,
    controller,
    bindings
};


const id = "waltzEditableEnum";


export default {
    component,
    id
};