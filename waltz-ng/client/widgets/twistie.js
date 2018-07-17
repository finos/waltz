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

import {initialiseData} from "../common";
import template from './twistie.html';


const bindings = {
    collapsed: '<',
    toggleExpansion: '<'
};


const initialState = {
    collapsed: false,
    toggleExpansion: (collapsed) => console.log("Default handler defined for toggleExpansion - collapsed: ", collapsed)
};


function calcClassNames(collapsed = false) {
    const iconName = collapsed
        ? 'chevron-right'
        : 'chevron-down';

    return [
        'fa',
        'fa-fw',
        `fa-${ iconName }`
    ];
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.onClick = () => {
        vm.collapsed = ! vm.collapsed;
        if (vm.toggleExpansion) {
            vm.toggleExpansion(vm.collapsed);
        }
    };

    vm.$onChanges = () => {
        vm.clazz = calcClassNames(vm.collapsed);
    };

}


const component = {
    bindings,
    controller,
    template
};


export default component;


