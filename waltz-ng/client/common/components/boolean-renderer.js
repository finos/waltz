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
import _ from "lodash";
import {initialiseData} from "../../common";


const bindings = {
    value: '<'
};


const template = `<waltz-icon ng-if="$ctrl.value === true" name="check" class="text-success"></waltz-icon>
                  <waltz-icon ng-if="$ctrl.value === false" name="times" class="text-danger"></waltz-icon>
                  <span ng-if="$ctrl.value == null" class="text-muted">-</span>`;


const initialState = {
    booleanValue: null
};


const trueStrings = ['TRUE', 'YES'];


const falseStrings = ['FALSE', 'NO'];


function checkStr(str, collection) {
    return str
        && _.isString(str)
        && _.includes(collection, _.upperCase(str));
}


function isTrueString(str) {
    return checkStr(str, trueStrings);
}


function isFalseString(str) {
    return checkStr(str, falseStrings);
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        if (vm.value === true || isTrueString(vm.value)) {
            vm.booleanValue = true;
        } else if (vm.value === false || isFalseString(vm.value)) {
            vm.booleanValue = false;
        } else {
            vm.booleanValue = null;
        }
    };
}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;