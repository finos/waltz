/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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

import { initialiseData, invokeFunction } from "../../../common";
import { FILTER_CHANGED_EVENT } from "../../constants";

const bindings = {
    onFiltersChanged: "<",
};


const initialState = {
    filters: {},

    onFiltersChanged: (filters) => console.log("wfcw - filters changed: ", filters)
};


function controller($scope) {
    const vm = initialiseData(this, initialState);

    const filterChangedListener = $scope.$on(FILTER_CHANGED_EVENT, (event, data) => {
        vm.filters = Object.assign({}, data);
        invokeFunction(vm.onFiltersChanged, vm.filters);
    });


    vm.$onDestroy = () => {
        // unsubscribe
        filterChangedListener();
    };
}


controller.$inject = [
    "$scope"
];


const component = {
    template: "",
    bindings,
    controller
};


export default {
    component,
    id: "waltzFilterChangeWatcher"
};
