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

import { CORE_API } from "../../../common/services/core-api-utils";
import { initialiseData } from "../../../common";
import { mkSelectionOptions } from "../../../common/selector-utils";

import template from "./change-set-section.html";


const bindings = {
    parentEntityRef: "<",
};


const initialState = {
    changeSets: [],
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        if(changes.parentEntityRef) {
            serviceBroker
                .loadViewData(CORE_API.ChangeSetStore.findBySelector, [mkSelectionOptions(vm.parentEntityRef)])
                .then(r => vm.changeSets = r.data)
        }
    };

}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzChangeSetSection"
};
