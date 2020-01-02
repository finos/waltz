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
import {initialiseData} from "../../common/index";

import template from "./editable-dropdown.html";


const bindings = {
    initialVal: "<",
    entries: "<", // [ { code, name, position? }, ... ]
    commentsAllowed: "@?",
    initialComments: "<?",
    onSave: "<",
    onRemove: "<?",
    editRole: "@",
    emptyLabel: "@",
    ctx: "<?"
};


const initialState = {
    currentVal: null,
    currentComments: null,
    dropdownValues: [],
    dropdownsByCode: {},
    visibility: {
        editor: false
    }
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        vm.currentVal = vm.initialVal;
        vm.currentComments = vm.initialComments;

        if (vm.entries) {
            vm.dropdownValues = _.orderBy(vm.entries, ["position", "name"]);
            vm.dropdownsByCode = _.keyBy(vm.dropdownValues, "code");
        }
    };

    vm.onEdit = () => {
        vm.visibility.editor = true;
    };

    vm.doCancel = () => {
        vm.visibility.editor = false;
        vm.saving = false;
        vm.currentVal = vm.initialVal;
        vm.currentComments = vm.initialComments;
    };

    vm.doSave = () => {
        vm.saving = true;

        return vm
            .onSave(vm.currentVal, vm.currentComments, vm.ctx)
            .then(() => {
                vm.visibility.editor = false;
                vm.saving = false;
            });
    };

    vm.getName = (code) => {
        return _.get(vm, ["dropdownsByCode", code, "name"], code);
    };

    vm.isDirty = () => {
        return vm.currentVal != null && (vm.currentVal != vm.initialVal || vm.currentComments != vm.initialComments);
    };
}


controller.$inject = [
];


const component = {
    template,
    controller,
    bindings
};


const id = "waltzEditableDropdown";


export default {
    component,
    id
};