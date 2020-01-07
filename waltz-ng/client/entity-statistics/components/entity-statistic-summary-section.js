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

import {entityLifecycleStatuses, resetData} from "../../common";
import {CORE_API} from "../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../common/selector-utils";

import template from "./entity-statistic-summary-section.html";


const bindings = {
    filters: "<",
    parentEntityRef: "<"
};


const initData = {
    activeDefinition: null,
    activeSummary: null,
    definitions: [],
    loading: false,
    summaries: {}
};


function controller(serviceBroker) {
    const vm = resetData(this, initData);

    const loadStatTallies = (definition) => {
        vm.loading = true;
        const selectionOptions = mkSelectionOptions(
            vm.parentEntityRef,
            undefined,
            [
                entityLifecycleStatuses.ACTIVE,
                entityLifecycleStatuses.PENDING,
                entityLifecycleStatuses.REMOVED
            ],
            vm.filters);
        serviceBroker
            .loadViewData(CORE_API.EntityStatisticStore.findStatTallies, [[definition.id], selectionOptions])
            .then(result => {
                vm.loading = false;
                vm.summaries[definition.id] = result.data[0];
                vm.activeSummary = result.data[0];
            });
    };

    serviceBroker
        .loadAppData(CORE_API.EntityStatisticStore.findAllActiveDefinitions, [])
        .then(result => vm.definitions = result.data);

    vm.onDefinitionSelection = (d) => {
        vm.activeDefinition = d;
        if (vm.summaries[d.id]) {
            vm.loading = false;
            vm.activeSummary = vm.summaries[d.id];
        } else {
            loadStatTallies(d)
        }
    };


    vm.$onChanges = (changes) => {
        if(vm.activeDefinition && changes.filters) {
            loadStatTallies(vm.activeDefinition);
        }
    };
}


controller.$inject = [
    "ServiceBroker"
];


export default {
    component: {
        template,
        bindings,
        controller
    },
    id: "waltzEntityStatisticSummarySection"
};

