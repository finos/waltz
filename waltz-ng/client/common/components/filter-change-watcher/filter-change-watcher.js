/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

import { initialiseData, invokeFunction } from "../../index";
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

    const filterChangedListenerDeregisterFn = $scope.$on(
        FILTER_CHANGED_EVENT,
        (event, data) => {
            vm.filters = Object.assign({}, data);
            invokeFunction(vm.onFiltersChanged, vm.filters);
        });


    vm.$onDestroy = () => {
        // unsubscribe
        filterChangedListenerDeregisterFn();
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
