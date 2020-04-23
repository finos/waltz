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
import template from "./org-unit-direct-measurable-section.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";

const bindings = {
    parentEntityRef: "<"
};

const initialState = {
    selectedCategory: {id: 9, name: "Process" },
    activeTab: null,
    tabs: []
};

function controller(serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        serviceBroker
            .loadViewData(CORE_API.MeasurableCategoryStore.findCategoriesByDirectOrgUnit,
                [vm.parentEntityRef.id])
            .then(r => vm.tabs = r.data)
            .then(() => vm.activeTab = _.first(vm.tabs));
    };

    vm.onTabChange = () => {
        if(_.isUndefined(vm.activeTab)){
            vm.activeTab = _.first(vm.tabs);
        }
    };

}

controller.$inject = [
    "ServiceBroker"
];


export default {
    component: {
        template,
        controller,
        bindings
    },
    id: "waltzOrgUnitDirectMeasurableSection"
}