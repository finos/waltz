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
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {entityLifecycleStatus} from "../../../common/services/enums/entity-lifecycle-status";

import template from "./logical-flows-tabgroup.html";
import {entity} from "../../../common/services/enums/entity";
import {loadFlowClassificationRatings} from "../../../flow-classification-rule/flow-classification-utils";
import FlowDetailPanel from "../../../data-flow/components/svelte/flow-detail-tab/FlowDetailPanel.svelte";


const bindings = {
    filters: "<",
    parentEntityRef: "<"
};

const initialState = {
    flows: [],
    decorators: [],
    visibility: {
        loadingFlows: false,
        loadingStats: false
    },
    FlowDetailPanel,
    currentTabIndex: 0
};

function controller($q,
                    serviceBroker) {

    const vm = _.defaultsDeep(this, initialState);

    const loadDetail = () => {
        vm.visibility.loadingFlows = true;

        const flowPromise = serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.findBySelector,
                [ vm.selector ])
            .then(r => vm.flows = r.data);

        const decoratorPromise = serviceBroker
            .loadViewData(
                CORE_API.DataTypeDecoratorStore.findBySelector,
                [ vm.selector, entity.LOGICAL_DATA_FLOW.key ])
            .then(r => {
                vm.decorators = r.data;
            });

        return $q
            .all([flowPromise, decoratorPromise])
            .then(() => {
                vm.visibility.loadingFlows = false;
            });
    };

    const loadStats = () => {
        vm.loadingStats = true;
        serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.calculateStats,
                [ vm.selector ])
            .then(r => {
                vm.loadingStats = false;
                vm.stats = r.data;
            });
    };

    vm.tabSelected = (tabName, index) => {
        if(index > 0) {
            loadDetail();
        }
        if(index === 1) {
            vm.visibility.boingyEverShown = true;
        }
        vm.currentTabIndex = index;
    };

    vm.$onChanges = (changes) => {
        loadFlowClassificationRatings(serviceBroker)
            .then(xs => vm.flowClassifications = xs);

        if (vm.parentEntityRef) {
            vm.selector = mkSelectionOptions(
                vm.parentEntityRef,
                undefined,
                [entityLifecycleStatus.ACTIVE.key],
                vm.filters);
            loadStats();
        }

        if(changes.filters) {
            if(vm.currentTabIndex > 0) {
                loadDetail();
            } else {
                loadStats();
            }
        }
    };

}


controller.$inject = [
    "$q",
    "ServiceBroker"
];


const component = {
    controller,
    bindings,
    template
};


export default component;
