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
import {mkSelectionOptions} from "../../../common/selector-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import {entityLifecycleStatus} from "../../../common/services/enums/entity-lifecycle-status";

import template from "./apps-section.html";


const bindings = {
    parentEntityRef: "<",
    filters: "<"
};


const initialState = {
    apps: [],
    endUserApps: [],
    combinedCount: 0,
    visibility:{
        tab: "SUMMARY"
    }
};


const DEFAULT_APP_SETTINGS = {
    management: "End User",
    kind: "EUC",
    overallRating: "Z"
};


function combine(apps = [], endUserApps = []) {
    return _.concat(apps, endUserApps);
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const refresh = () => {
        vm.combinedCount = _.size(vm.apps) + _.size(vm.endUserApps);
    };

    const loadAll = () => {
        vm.selectorOptions = mkSelectionOptions(
            vm.parentEntityRef,
            undefined,
            [entityLifecycleStatus.ACTIVE.key],
            vm.filters);

        serviceBroker
            .loadViewData(CORE_API.ApplicationStore.findBySelector, [vm.selectorOptions])
            .then(r => r.data)
            .then(apps => vm.apps = _.map(
                apps,
                a => Object.assign({}, a, {management: "IT"})))
            .then(refresh);

        if (vm.parentEntityRef.kind === "ORG_UNIT") {
            serviceBroker
                .loadViewData(CORE_API.EndUserAppStore.findBySelector, [vm.selectorOptions])
                .then(r => r.data)
                .then(endUserApps => vm.endUserApps = _.map(
                    endUserApps,
                    a => Object.assign({}, a, DEFAULT_APP_SETTINGS, { platform: a.applicationKind })))
                .then(refresh);
        }
    };


    vm.$onInit = () => {
        loadAll();
    };

    vm.$onChanges = (changes) => {
        if(changes.filters) {
            loadAll();
        }
    };

    vm.onTabSelect = (tabName) => {
        vm.visibility.tab = tabName;
        if (tabName === "DETAIL") {
            vm.combinedApps = combine(vm.apps, vm.endUserApps);
        }
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
    id: "waltzAppsSection",
    component
};