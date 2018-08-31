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
            vm.dropdownValues = _.orderBy(vm.entries, ['position', 'name']);
            vm.dropdownsByCode = _.keyBy(vm.dropdownValues, 'code');
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
        return _.get(vm, ['dropdownsByCode', code, 'name'], code);
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


const id = 'waltzEditableDropdown';


export default {
    component,
    id
};