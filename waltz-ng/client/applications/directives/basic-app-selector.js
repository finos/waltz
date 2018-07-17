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
import {invokeFunction} from "../../common";
import template from './basic-app-selector.html';


const BINDINGS = {
    addLabel: '@',
    cancelLabel: '@',
    onCancel: '<',
    onAdd: '<',
    onSelect: '<'
};


const initialState = {
    addLabel: 'Add',
    cancelLabel: 'Cancel',
    onCancel: () => console.log('No onCancel provided to basic app selector'),
    onAdd: (a) => console.log('No onAdd provided to basic app selector', a),
    onSelect: (a) => console.log('No onSelect provided to basic app selector', a)
};


function controller() {
    const vm = _.defaultsDeep(this, initialState);

    vm.add = (app) => {
        if (! app) return ;
        vm.onAdd(app);
    };

    vm.cancel = () => vm.onCancel();

    vm.select = (app) => invokeFunction(vm.onSelect, app);
}


controller.$inject = [];


const directive = {
    restrict: 'E',
    replace: false,
    template,
    scope: {},
    bindToController: BINDINGS,
    controller,
    controllerAs: 'ctrl'
};


export default () => directive;


