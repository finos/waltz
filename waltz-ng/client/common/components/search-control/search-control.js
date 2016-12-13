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

import { initialiseData, invokeFunction } from "../../../common"
import _ from 'lodash';


const bindings = {
    onQuery: '<',
    minCharacters: '<',
    delay: '<',
    placeholderText: '@'
}


const template = require('./search-control.html');


const initialState = {
    minCharacters: 3,
    delay: 250,
    placeholderText: 'Search...',
    onQuery: query => console.log('default onQuery handler in search-control: ', query),
}


function controller($scope) {
    const vm = initialiseData(this, initialState);

    vm.options = {
        debounce: vm.delay
    };


    $scope.$watch('$ctrl.query', q => {
        if(_.isString(q) && (q.length >= vm.minCharacters || q === "")) {
            invokeFunction(vm.onQuery, q);
        }
    });

}


controller.$inject = ["$scope"];


const component = {
    bindings,
    template,
    controller
};


export default component;