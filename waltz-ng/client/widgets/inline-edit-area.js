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
