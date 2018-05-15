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

import '@fortawesome/fontawesome-free/js/all'
import {mkCurrentName, mkIconSetName} from './icon-utils';
import {initialiseData} from "../common/index";

const bindings = {
    name: '@',
    iconSet: '@',
    size: '@',
    flip: '@',
    rotate: '@',
    stack: '@',
    fixedWidth: '@',
    inverse: '@',
    spin: '@'
};

const initialState = {
    iconSet: 'fa'
};


const template = '<span style="font-size: smaller; opacity: 0.8;"><i ng-class="$ctrl.classNames"/></span>';


function controller() {
    const vm = initialiseData(this, initialState);
    vm.$onChanges = () => {
        vm.classNames = [
            mkIconSetName(vm.iconSet, vm.name),
            mkCurrentName(vm.name),
            vm.flip ? `fa-flip-${vm.flip}` : '',
            vm.rotate ? `fa-rotate-${vm.rotate}` : '',
            vm.size ? `fa-${vm.size}` : '',
            vm.stack ? `fa-stack-${vm.stack}` : '',
            vm.fixedWidth ? 'fa-fw' : '',
            vm.inverse ? 'fa-inverse' : '',
            vm.spin ? 'fa-spin' : ''
        ].join(' ');
    }
}


const component = {
    bindings,
    template,
    controller
};


export default component;


