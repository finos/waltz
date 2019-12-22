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
import _ from "lodash";
import {initialiseData} from "../../common";
import {mkSelectionOptions} from "../../common/selector-utils";
import {CORE_API} from "../../common/services/core-api-utils";

import template from "./technology-summary-section.html";


const bindings = {
    filters: "<",
    parentEntityRef: "<"
};


const initialState = {
    stats: {},
    hasAnyData: false
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadAll = () => {
        const selector = mkSelectionOptions(
            vm.parentEntityRef,
            undefined,
            undefined,
            vm.filters);

        serviceBroker
            .loadViewData(CORE_API.TechnologyStatisticsService.findBySelector, [selector])
            .then(r => {
                vm.stats = r.data;
                const {serverStats = {}, databaseStats = {}, softwareStats = {}} = vm.stats;

                const hasServerStats = (serverStats && !_.isEmpty(serverStats.environmentCounts));
                const hasDbStats = (databaseStats && !_.isEmpty(databaseStats.environmentCounts));
                const hasSwStats = (softwareStats && !_.isEmpty(softwareStats.vendorCounts));

                vm.hasAnyData = hasServerStats || hasDbStats || hasSwStats;
            });
    };


    vm.$onInit = () => {
        loadAll();
    };


    vm.$onChanges = (changes) => {
        if(changes.filters) {
            loadAll();
        }
    };
}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    bindings,
    template,
    controller
};


const id = "waltzTechnologySummarySection";


export default {
    id,
    component
};