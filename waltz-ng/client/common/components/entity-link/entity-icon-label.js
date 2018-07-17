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
import template from './entity-icon-label.html';


const bindings = {
    entityRef: '<',
    iconPlacement: '@',
    tooltipPlacement: '@'
};



const initialState = {
    iconPlacement: 'left', // can be left, right, none
    tooltipPlacement: 'top', // left, top-left, top-right; refer to: (https://github.com/angular-ui/bootstrap/tree/master/src/tooltip)
    trigger: 'none'
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChange = c => {
        vm.trigger = vm.entityRef.description || vm.entityRef.lifecyclePhase
            ? 'mouseenter'
            : 'none';
    };
}


const component = {
    bindings,
    template,
    controller
};


export default component;