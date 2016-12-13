/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import {initialiseData} from '../common';

const template = require('./editable-field.html');


const bindings = {
    initialVal: '<',
    onSave: '<',
    fieldType: '@',
    itemId: '<'
};


const initialState = {
    errorMessage: "",
    editing: false,
    saving: false,
    fieldType: 'text',
    onSave: () => console.log("WETF: No on-save method provided")
};


function controller($timeout) {
    const vm = initialiseData(this, initialState);


    const saveComplete = () => {
        vm.saving = false;
        vm.editing = false;
        vm.errorMessage = "";
    };

    const saveFailed = (e) => {
        vm.saving = false;
        vm.editing = true;
        vm.errorMessage = e;
    };


    vm.save = () => {
        const data = {
            newVal: vm.newVal,
            oldVal: vm.initialVal
        };

        vm.saving = true;

        const promise = vm.onSave(vm.itemId, data);

        if (promise) {
            promise.then(saveComplete, saveFailed)
        } else {
            saveComplete();
        }
    };


    vm.edit = () => {
        vm.editing = true;
        vm.newVal = vm.initialVal;
    };


    vm.cancel = () => {
        vm.editing = false;
        vm.saving = false;
        vm.errorMessage = "";
    };

}


controller.$inject = ['$timeout'];


const component = {
    template,
    bindings,
    controller
};


export default component;