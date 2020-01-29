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

import {initialiseData} from "../../../common";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {CORE_API} from "../../../common/services/core-api-utils";

import template from "./simple-app-table.html";


const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    apps: []
};


function controller($scope,
                    serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        vm.selectorOptions = mkSelectionOptions(
            vm.parentEntityRef,
            "EXACT");

        serviceBroker
            .loadViewData(CORE_API.ApplicationStore.findBySelector, [vm.selectorOptions])
            .then(r => vm.apps = r.data);
    };
}


controller.$inject = [
    "$scope",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default component;