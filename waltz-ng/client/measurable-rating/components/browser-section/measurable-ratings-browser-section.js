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
import template from "./measurable-ratings-browser-section.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";

/**
 * @name waltz-measurable-ratings-browser
 *
 * @description
 * This component ...
 */


const bindings = {
    filters: "<",
    parentEntityRef: "<"
};


const initialState = {
    hasAllocations: true
};



function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.AllocationSchemeStore.findAll)
            .then(r => vm.schemesByCategoryId = _.groupBy(r.data, s => s.measurableCategoryId));

        vm.selector = mkSelectionOptions(vm.parentEntityRef,
            undefined,
            undefined,
            vm.filters);
    };

    vm.onCategorySelect = (category) => {
        vm.activeCategory = category;
        vm.hasAllocations = _.has(vm.schemesByCategoryId, category.id);
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
    id: "waltzMeasurableRatingsBrowserSection"
};