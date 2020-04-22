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
import template from "./playpen4.html";
import {CORE_API} from "../../common/services/core-api-utils";

const initialState = {
    parentEntityRef: { kind: "ORG_UNIT", id: 6811 }, //10524
    selectedCategoryId: 9
};



function controller($q, serviceBroker) {

    const vm = initialiseData(this, initialState);

    serviceBroker
        .loadViewData(CORE_API.MeasurableCategoryStore.getById, [ vm.selectedCategoryId ])
        .then(r => vm.selectedCategory = r.data);
}


controller.$inject = [
    "$q",
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
