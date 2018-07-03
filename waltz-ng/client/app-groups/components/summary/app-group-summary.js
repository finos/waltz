/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from 'lodash';
import {enrichServerStats} from "../../../server-info/services/server-utilities";
import {calcComplexitySummary} from "../../../complexity/services/complexity-utilities";
import template from './app-group-summary.html';
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";


const bindings = {
    parentEntityRef: '<'
};


const initialState = {
    editable: false,
    isSubscribed: false,
};


function determineSubscriptionStatus(isOwner = false, isSubscriber = false) {
    if (isOwner) {
        // we don't want owners to 'abandon' their app groups
        return 'NOT_APPLICABLE';
    } else {
        return isSubscriber ? 'SUBSCRIBED' : 'UNSUBSCRIBED';
    }
}


function controller($q, serviceBroker, userService) {

    const vm = initialiseData(this, initialState);

    const reloadPermissionsEtc = () => {
        const containsCurrentUser = (otherUsers) => _
            .chain(otherUsers)
            .map('userId')
            .includes(vm.user.userName)
            .value();

        const isOwner = containsCurrentUser(vm.owners);
        const isSubscriber = containsCurrentUser(vm.members);
        vm.subscriptionStatus = determineSubscriptionStatus(isOwner, isSubscriber);
        vm.editable = isOwner;
    };

    vm.$onInit = () => {
        const selector = mkSelectionOptions(vm.parentEntityRef);

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
                vm.owners = _.filter(vm.members, { role: 'OWNER' });
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
                .then(r => vm.subscriptionStatus = 'SUBSCRIBED');
        };

        vm.onUnsubscribe = () => {
            serviceBroker
                .execute(
                    CORE_API.AppGroupStore.unsubscribe,
                    [vm.parentEntityRef.id])
                .then(() => vm.subscriptionStatus = 'UNSUBSCRIBED');
        };

    };

}

controller.$inject = [
    '$q',
    'ServiceBroker',
    'UserService'
];


const component = {
    controller,
    template,
    bindings
};

export default {
    component,
    id: 'waltzAppGroupSummary'
}
