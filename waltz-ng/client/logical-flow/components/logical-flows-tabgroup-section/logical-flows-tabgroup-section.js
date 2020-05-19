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
import {mkSelectionOptions} from "../../../common/selector-utils";
import {entityLifecycleStatus} from "../../../common/services/enums/entity-lifecycle-status";

import template from "./logical-flows-tabgroup-section.html";
import {CORE_API} from "../../../common/services/core-api-utils";


const bindings = {
    filters: "<",
    parentEntityRef: "<"
};


const initialState = {
    export: () => console.log("lfts: default do-nothing export function"),
    visibility: {
        exportButton: false,
        sourcesOverlay: false
    }
};


function calcHasFlows(stats) {
    const counts = _.get(stats, "flowCounts", {});
    const total = _.sum(_.values(counts));
    return total > 0;
}


function controller(serviceBroker) {
    const vm = _.defaultsDeep(this, initialState);

    const load = (selector) => {
        vm.loadingStats = true;

        serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.calculateStats,
                [ selector ])
            .then(r => {
                vm.loadingStats = false;
                vm.stats = r.data;
                vm.hasFlows = calcHasFlows(vm.stats);
            });
    };

    vm.$onInit = () => {
    };

    vm.$onChanges = (changes) => {

        if (vm.parentEntityRef) {
            vm.selector = mkSelectionOptions(
                vm.parentEntityRef,
                undefined,
                [entityLifecycleStatus.ACTIVE.key],
                vm.filters);

            load(vm.selector);
        }

        if(changes.filters) {
            load(vm.selector);
        }
    };

}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    controller,
    bindings,
    template
};


export default component;