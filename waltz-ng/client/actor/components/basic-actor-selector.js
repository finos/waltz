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

import {initialiseData, invokeFunction} from "../../common";
import template from './basic-actor-selector.html';


const bindings = {
    allActors: '<',
    addLabel: '@',
    cancelLabel: '@',
    onCancel: '<',
    onAdd: '<',
    onSelect: '<'
};




const initialState = {
    addLabel: 'Add',
    cancelLabel: 'Cancel',
    onCancel: () => console.log('No onCancel provided to basic actor selector'),
    onAdd: (a) => console.log('No onAdd provided to basic actor selector', a),
    onSelect: (a) => console.log('No onSelect provided to basic actor selector', a)
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.add = (actor) => {
        if (! actor) return ;
        vm.onAdd(actor);
    };

    vm.cancel = () => vm.onCancel();

    vm.select = (actor) => {
        vm.selectedActor = actor;
        invokeFunction(vm.onSelect, actor);
    };
}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;


