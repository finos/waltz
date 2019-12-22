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
import { initialiseData } from "../../../common";
import { mkSelectionOptions } from "../../../common/selector-utils";
import { CORE_API } from "../../../common/services/core-api-utils";
import { entityLifecycleStatus } from "../../../common/services/enums/entity-lifecycle-status";

import template from "./apps-section.html";


const bindings = {
    parentEntityRef: "<",
    filters: "<"
};


const initialState = {
    apps: [],
    endUserApps: [],
};


const DEFAULT_APP_SETTINGS = {
    management: "End User",
    kind: "EUC",
    overallRating: "Z"
};


function combine(apps = [], endUserApps = []) {
    return _.concat(apps, endUserApps);
}


function controller($scope,
                    serviceBroker) {
    const vm = initialiseData(this, initialState);

    const refresh = () => {
        vm.combinedApps = combine(vm.apps, vm.endUserApps);
    };

    const loadAll = () => {
        const selectorOptions = mkSelectionOptions(
            vm.parentEntityRef,
            undefined,
            [entityLifecycleStatus.ACTIVE.key],
            vm.filters);

        serviceBroker
            .loadViewData(CORE_API.ApplicationStore.findBySelector, [selectorOptions])
            .then(r => r.data)
            .then(apps => vm.apps = _.map(
                apps,
                a => _.assign(a, { management: "IT" })))
            .then(refresh);

        if (vm.parentEntityRef.kind === "ORG_UNIT") {
            serviceBroker
                .loadViewData(CORE_API.EndUserAppStore.findBySelector, [selectorOptions])
                .then(r => r.data)
                .then(endUserApps => vm.endUserApps = _.map(
                    endUserApps,
                    a => _.assign(a, { platform: a.applicationKind }, DEFAULT_APP_SETTINGS)))
                .then(refresh);
        }
    };


    vm.$onInit = () => {
        loadAll();
    };

    vm.$onChanges = (changes) => {
        vm.combinedApps = combine(vm.apps, vm.endUserApps);

        if(changes.filters) {
            loadAll();
        }
    };

    vm.onInitialise = (cfg) => {
        vm.export = () => cfg.exportFn("apps.csv");
    };
}


controller.$inject = [
    "$scope",
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