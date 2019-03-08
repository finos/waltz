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

import {initialiseData, invokeFunction} from "../../../common"
import _ from 'lodash';
import template from './search-control.html';


const bindings = {
    onQuery: '<',
    minCharacters: '<',
    delay: '<',
    placeholderText: '@',
    localStorageKey: '@?'
};


const initialState = {
    minCharacters: 3,
    delay: 250,
    placeholderText: 'Search...',
    onQuery: query => console.log('default onQuery handler in search-control: ', query),
};


function mkStorageKey(localStorageKeyStem = "") {
    return localStorageKeyStem + ".search";
}


function controller($scope, localStorageService) {
    const vm = initialiseData(this, initialState);

    vm.options = {
        debounce: vm.delay
    };

    vm.$onInit = () => {
        if (vm.localStorageKey) {
            const key = mkStorageKey(vm.localStorageKey);
            vm.query = localStorageService.get(key) || "";
            invokeFunction(vm.onQuery, vm.query);
        }
    };

    $scope.$watch('$ctrl.query', q => {
        if(_.isString(q) && (q.length >= vm.minCharacters || q === "")) {
            invokeFunction(vm.onQuery, q);
            if (vm.localStorageKey) {
                const key = mkStorageKey(vm.localStorageKey);
                localStorageService.set(key, q);
            }
        }
    });

}


controller.$inject = [
    "$scope",
    "localStorageService"
];


const component = {
    bindings,
    template,
    controller
};


export default component;