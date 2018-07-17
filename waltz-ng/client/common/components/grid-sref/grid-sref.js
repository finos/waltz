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
import {initialiseData} from "../../../common";
import template from './grid-sref.html';


const bindings = {
    state: '<',
    params: '<',
    linkText: '@'
};


const initialState = {
    linkText: 'Visit'
};


function controller($state) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        if (vm.state && vm.params) {
            const paramsObj = _.isString(vm.params) ? JSON.parse(vm.params) : vm.params;
            // console.log('state link: ', vm.state, paramsObj)
            // url needs to be re-computed when entityRef changes
            // eg: when used in a ui-grid cell template
            vm.viewUrl = $state.href(vm.state, paramsObj);
        }
    };
}


controller.$inject = [
    '$state'
];


const component = {
    bindings,
    template,
    controller
};


export default component;