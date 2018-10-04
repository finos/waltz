/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
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

import {initialiseData} from "../../common/index";
import {toOptions} from "../../common/services/enums";

import template from "./editable-enum.html";


const bindings = {
    initialVal: "<",
    enumKind: "@",
    onSave: "<",
    editRole: "@",
    ctx: "<?"
};


const initialState = {
    currentVal: null,
    visibility: {
        editor: false
    }
};


function controller(displayNameService, enumValueService) {
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

}

controller.$inject = [
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