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

import {initialiseData} from "../../../common";
import template from './date-picker-form-input.html';


const bindings = {
    id: '@',
    placeHolder: '@',
    required: '@',
    format: '@',
    allowPastDates: '@',
    model: '=',
    itemId: '<',
    onChange: '<'
};




const initialState = {
    dateOptions: {
        formatYear: 'yyyy',
        startingDay: 1
    },
    datePickerOpened: false,
    placeHolder: '',
    onChange: (itemId, val) => console.log("default onChange ")
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        if (!vm.allowPastDates) {
            vm.dateOptions.minDate = new Date();
        }
    };

    vm.datePickerOpen = () => {
        vm.datePickerOpened = true;
    };

    vm.valueChanged = () => {
        vm.onChange(vm.itemId, vm.model);
    };
}


const component = {
    bindings,
    template,
    controller
};


export default component;