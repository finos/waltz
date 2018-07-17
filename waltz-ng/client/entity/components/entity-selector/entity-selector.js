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

import {initialiseData, invokeFunction} from "../../../common";
import _ from 'lodash';
import template from './entity-selector.html';


const bindings = {
    clearable: '<',
    currentSelection: '<',
    entityKinds: '<',
    itemId: '<',
    limit: '<',
    onSelect: '<',
    required: '<',
    selectionFilter: '<'
};


const initialState = {
    entities: [],
    limit: 20,
    entityKinds: [],
    required: false,
    selectionFilter: (x) => true
};


function controller(entitySearchStore) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        vm.options = {
            entityKinds: vm.entityKinds,
            limit: vm.limit,
            entityLifecycleStatuses: ['ACTIVE', 'PENDING', 'REMOVED']
        };

        if (changes.entityKinds) {
            vm.entities = [];
        }

        if (vm.currentSelection) {
            vm.refresh(vm.currentSelection.name, vm.options);
        }
    };

    vm.refresh = function(query) {
        if (!query) return;
        return entitySearchStore.search(query, vm.options)
            .then((entities) => {
                vm.entities = vm.selectionFilter
                    ? _.filter(entities, vm.selectionFilter)
                    : entities;
            });
    };

    vm.select = (item) => invokeFunction(vm.onSelect, item, vm.itemId);

    vm.mkTracker = (item) => item.kind + "_" + item.id;
}


controller.$inject = ['EntitySearchStore'];


const component = {
    bindings,
    template,
    controller,
};


export default component;


