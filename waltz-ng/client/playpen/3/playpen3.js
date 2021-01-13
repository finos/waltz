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

import {initialiseData} from "../../common/index";
import template from "./playpen3.html";
import {CORE_API} from "../../common/services/core-api-utils";


const initialState = {
    parentEntityRef: {
        id: 20506,
        kind: "APPLICATION"
    },
    schemeId: 2,
    selectedDate: null,
};

function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    serviceBroker
        .loadViewData(CORE_API.CostKindStore.findAll)
        .then(r => vm.costKinds = r.data)
        .then(console.log(vm.costKinds));

    serviceBroker
        .loadViewData(CORE_API.CostStore.findByEntityReference, [ vm.parentEntityRef ])
        .then(r => vm.costs = r.data)
        .then(console.log(vm.costs));
}

controller.$inject = [
    "ServiceBroker"
];


const view = {
    template,
    controller,
    controllerAs: "$ctrl",
    bindToController: true,
    scope: {}
};


export default view;
