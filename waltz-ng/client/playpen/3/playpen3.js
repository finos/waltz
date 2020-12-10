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
        id: 95,
        kind: "ORG_UNIT"
    },
    schemeId: 2,
    selectedDate: null,
};

function controller($stateParams, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.selectedDate = new Date('2020-07-07');

    // serviceBroker.loadViewData(CORE_API.ChangeLogSummariesStore.findSummariesForKindBySelector,
    //     ['APPLICATION', mkSelectionOptions(vm.parentEntityRef, 'EXACT')])
    //     .then(r => vm.data = r.data)
    //     .then(console.log(vm.data));
}

controller.$inject = [
    "$stateParams",
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
