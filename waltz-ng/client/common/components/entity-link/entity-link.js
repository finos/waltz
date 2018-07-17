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
import {kindToViewState} from "../../../common/link-utils";
import template from './entity-link.html';


const bindings = {
    entityRef: '<',
    iconPlacement: '@',
    tooltipPlacement: '@',
    target: '@', /** if '_blank' the external icon is shown **/
};


const initialState = {
    iconPlacement: 'left', // can be left, right, none
    tooltipPlacement: 'top' // left, top-left, top-right; refer to: (https://github.com/angular-ui/bootstrap/tree/master/src/tooltip)
};


function controller($state) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        if (vm.entityRef) {
            const viewState = kindToViewState(vm.entityRef.kind);
            if (viewState) {
                // url needs to be re-computed when entityRef changes
                // eg: when used in a ui-grid cell template
                vm.viewUrl = $state.href(viewState, { id: vm.entityRef.id });
            }
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