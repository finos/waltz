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


const initialState = {
    parentEntityRef: {
        id: 2732,
        kind: "ORG_UNIT",
        name: "A Group"
    },
    parentEntityRef2: {
        // id: 20506,
        id: 20506,
        kind: "APPLICATION",
        name: "An app"
    },
    // schemeId: 2,
    // selectedDate: null,
};

function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    //
    // const opts = mkSelectionOptions(vm.parentEntityRef);
    //
    // serviceBroker
    //     .loadViewData(CORE_API.CostKindStore.findAll)
    //     .then(r => vm.costKinds = r.data)
    //     .then(() => console.log(vm.costKinds));
    //
    // serviceBroker
    //     .loadViewData(CORE_API.CostStore.findByEntityReference, [ vm.parentEntityRef ])
    //     .then(r => vm.costs = r.data)
    //     .then(() => console.log(vm.costs));
    //
    // serviceBroker
    //     .loadViewData(CORE_API.CostStore.findBySelector, [ 'APPLICATION', opts ])
    //     .then(r => vm.costsForEntity = r.data)
    //     .then(() => console.log(vm.costsForEntity));
    //
    // serviceBroker
    //     .loadViewData(CORE_API.CostStore.findByCostKindAndSelector, [ 1, 'APPLICATION', opts ])
    //     .then(r => vm.costsOfKindForEntity = r.data)
    //     .then(() => console.log(vm.costsOfKindForEntity));
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
