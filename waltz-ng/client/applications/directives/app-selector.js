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

import {initialiseData, invokeFunction} from "../../common";
import template from './app-selector.html';


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
    template,
    scope: {},
    bindToController: BINDINGS,
    controller,
    controllerAs: 'ctrl'
};


export default () => directive;


