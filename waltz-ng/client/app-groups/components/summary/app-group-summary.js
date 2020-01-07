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
import {enrichServerStats} from "../../../server-info/services/server-utilities";
import {calcComplexitySummary} from "../../../complexity/services/complexity-utilities";
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {hierarchyQueryScope} from "../../../common/services/enums/hierarchy-query-scope";
import {entityLifecycleStatus} from "../../../common/services/enums/entity-lifecycle-status";

import template from "./app-group-summary.html";


const bindings = {
    parentEntityRef: "<",
    filters: "<"
};


const initialState = {
    editable: false,
    filters: {},
    isSubscribed: false,
};


function determineSubscriptionStatus(isOwner = false, isSubscriber = false) {
    if (isOwner) {
        // we don't want owners to 'abandon' their app groups
        return "NOT_APPLICABLE";
    } else {
        return isSubscriber ? "SUBSCRIBED" : "UNSUBSCRIBED";
    }
}


function controller($q, serviceBroker, userService) {

    const vm = initialiseData(this, initialState);

    const reloadPermissionsEtc = () => {
        const containsCurrentUser = (otherUsers) => _
            .chain(otherUsers)
            .map("userId")
            .includes(vm.user.userName)
            .value();

        const isOwner = containsCurrentUser(vm.owners);
        const isSubscriber = containsCurrentUser(vm.members);
        vm.subscriptionStatus = determineSubscriptionStatus(isOwner, isSubscriber);
        vm.editable = isOwner;
    };


    const loadAll = () => {
        const selector = mkSelectionOptions(
            vm.parentEntityRef,
            hierarchyQueryScope.EXACT.key,
            [entityLifecycleStatus.ACTIVE.key],
            vm.filters);

        const userPromise = userService
            .whoami()
            .then(u => vm.user = u);

        const groupPromise = serviceBroker
            .loadViewData(
                CORE_API.AppGroupStore.getById,
                [ vm.parentEntityRef.id ])
            .then(r => {
                vm.appGroup = r.data.appGroup;
                vm.members = r.data.members;
                vm.owners = _.filter(vm.members, { role: "OWNER" });
            });

        $q.all([userPromise, groupPromise])
            .then(() => {
                reloadPermissionsEtc();
            });

        serviceBroker
            .loadViewData(
                CORE_API.AssetCostStore.findTotalCostForAppSelector,
                [ selector ])
            .then(r => vm.totalCost = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.ComplexityStore.findBySelector,
                [ selector ])
            .then(r => vm.complexitySummary = calcComplexitySummary(r.data));

        serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.calculateStats,
                [ selector ])
            .then(r => vm.flowStats = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.ApplicationStore.findBySelector,
                [ selector ])
            .then(r => vm.applications = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.TechnologyStatisticsService.findBySelector,
                [ selector ])
            .then(r => vm.enrichedServerStats = enrichServerStats(r.data.serverStats));

        vm.onSubscribe = () => {
            serviceBroker
                .execute(
                    CORE_API.AppGroupStore.subscribe,
                    [vm.parentEntityRef.id])
                .then(r => vm.subscriptionStatus = "SUBSCRIBED");
        };

        vm.onUnsubscribe = () => {
            serviceBroker
                .execute(
                    CORE_API.AppGroupStore.unsubscribe,
                    [vm.parentEntityRef.id])
                .then(() => vm.subscriptionStatus = "UNSUBSCRIBED");
        };
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
    "$q",
    "ServiceBroker",
    "UserService"
];


const component = {
    controller,
    template,
    bindings
};

export default {
    component,
    id: "waltzAppGroupSummary"
}
