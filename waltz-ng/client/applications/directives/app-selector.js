/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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


const BINDINGS = {
    model: '=',
    onSelect: '<'
};


const initialState = {
    apps: []
};


function controller(ApplicationStore) {
    const vm = initialiseData(this, initialState);

    vm.refresh = function(query) {
        if (!query) return;
        return ApplicationStore.search(query)
            .then((apps) => {
                vm.apps = apps;
            });
    };


    vm.select = (item) => invokeFunction(vm.onSelect, item);
}


controller.$inject = ['ApplicationStore'];


const directive = {
    restrict: 'E',
    replace: true,
    template: require('./app-selector.html'),
    scope: {},
    bindToController: BINDINGS,
    controller,
    controllerAs: 'ctrl'
};


export default () => directive;


