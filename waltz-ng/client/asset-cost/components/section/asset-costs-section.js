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
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {entityLifecycleStatus} from "../../../common/services/enums/entity-lifecycle-status";

import template from "./asset-costs-section.html";


const bindings = {
    filters: "<",
    parentEntityRef: "<",
    csvName: "@?",
};


const initialState = {
    scope: "CHILDREN",
    csvName: "asset-costs.csv",
    visibility: {
        summary: true,
        detail: false
    }
};


function processSelection(displayNameService, d) {
    if (!d) return null;

    const costTable = _.map(d.costs, (v, k) => ({ type: displayNameService.lookup("CostKind", k) || k, amount: v }));
    return Object.assign({}, d, { costTable });
}


function controller(serviceBroker,
                    displayNameService) {

    const vm = initialiseData(this, initialState);

    const loadTopAppCosts = () => {
        serviceBroker
            .loadViewData(
                CORE_API.AssetCostStore.findTopAppCostsByAppIdSelector,
                [ mkSelectionOptions(
                    vm.parentEntityRef,
                    undefined,
                    [entityLifecycleStatus.ACTIVE.key],
                    vm.filters) ])
            .then(r => vm.topCosts = r.data);
    };


    const loadDetailedAppCosts = () => {
        serviceBroker
            .loadViewData(
                CORE_API.AssetCostStore.findAppCostsByAppIdSelector,
                [ mkSelectionOptions(
                    vm.parentEntityRef,
                    undefined,
                    [entityLifecycleStatus.ACTIVE.key],
                    vm.filters) ])
            .then(r => vm.allCosts = r.data);
    };


    vm.onSummarySelect = (d) => vm.summarySelection = processSelection(displayNameService, d);

    vm.showSummary = () => {
        vm.visibility.summary = true;
        vm.visibility.detail = false;
    };

    vm.showDetail = () => {
        vm.visibility.summary = false;
        vm.visibility.detail = true;

        loadDetailedAppCosts();
    };

    vm.$onInit = () => {
        loadTopAppCosts();
    };

    vm.$onChanges = (changes) => {
        if(changes.filters) {
            loadTopAppCosts();

            if(vm.visibility.detail) {
                loadDetailedAppCosts();
            }
        }
    };
}


controller.$inject = [
    "ServiceBroker",
    "DisplayNameService"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzAssetCostsSection"
};