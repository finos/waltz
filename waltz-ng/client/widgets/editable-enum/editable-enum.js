/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import template from './editable-enum.html';
import {initialiseData} from "../../common/index";
import {toOptions} from "../../common/services/enums";

const bindings = {
    initialVal: "<",
    enumKind: "@",
    onSave: "<",
    editRole: "@",
    ctx: '<?'
};


const initialState = {
    currentVal: null,
    visibility: {
        editor: false
    }
};


function controller(displayNameService) {
    const vm = initialiseData(this, initialState);


    vm.$onChanges = () => {
        vm.currentVal = vm.initialVal;
        const enumValuesMap = displayNameService.lookupsByType[vm.enumKind];
        vm.enumValues = toOptions(enumValuesMap);
    };

    vm.onEdit = () => {
        vm.visibility.editor = true;
    };

    vm.doCancel = () => {
        vm.visibility.editor = false;
        vm.saving = false;
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

}

controller.$inject = ['DisplayNameService'];


const component = {
    template,
    controller,
    bindings
};


const id = 'waltzEditableEnum';


export default {
    component,
    id
};