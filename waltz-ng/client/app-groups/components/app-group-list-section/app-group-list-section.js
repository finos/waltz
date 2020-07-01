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
import template from "./app-group-list-section.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";

const bindings = {
    groupSubscriptions: "<"
};

const initialData = {
    showPublicSearch: false,
    availableGroups: [],
    recentlySubscribed: []
};


function removeUsedGroups(allGroups, existingSubscriptions) {
    const subscribedGroupIds = _.map(existingSubscriptions, "appGroup.id");
    return _.reject(allGroups, g => _.includes(subscribedGroupIds, g.id));
}


function controller(serviceBroker, notification, $scope, $state) {

    const vm = initialiseData(this, initialData);

    function loadPublicGroups() {
        serviceBroker.loadViewData(CORE_API.AppGroupStore.findPublicGroups)
            .then(r => r.data)
            .then(publicGroups => removeUsedGroups(publicGroups, vm.groupSubscriptions))
            .then(availableGroups => vm.availableGroups = availableGroups);
    }


    function subscribeToGroup(group) {
        return serviceBroker
            .execute(CORE_API.AppGroupStore.subscribe, [group.id])
            .then(r => {
                vm.recentlySubscribed = _
                    .chain(vm.recentlySubscribed)
                    .concat(group)
                    .uniqBy(g => g.id)
                    .value();
                return r.data;
            })
    }


    function unsubscribeFromGroup(subscription) {
        return serviceBroker
            .execute(CORE_API.AppGroupStore.unsubscribe, [subscription.appGroup.id])
            .then(r => r.data);
    }

    vm.createNewGroup = () => {
        serviceBroker
            .execute(CORE_API.AppGroupStore.createNewGroup)
            .then(r => {
                notification.success("New group created");
                $state.go("main.app-group.edit", { id: r.data })
            });
    };


    vm.unsubscribe = (subscription) => {
        unsubscribeFromGroup(subscription)
            .then(groupSubscriptions => vm.groupSubscriptions = groupSubscriptions)
            .then(() => notification.warning(`Unsubscribed from: ${subscription.appGroup.name}`));
    };


    vm.deleteGroup = (group) => {
        if (! confirm("Really delete this group ? \n " + group.appGroup.name)) return;

        serviceBroker
            .execute(CORE_API.AppGroupStore.deleteGroup, [group.appGroup.id])
            .then(r => r.data)
            .then(groupSubscriptions => vm.groupSubscriptions = groupSubscriptions)
            .then(() => notification.warning(`Deleted group: ${group.appGroup.name}`));

    };


    vm.onShowPublicSearch = () => {
        vm.showPublicSearch = true;
        loadPublicGroups();
    };


    vm.onHidePublicSearch = () => {
        vm.showPublicSearch = false;

    };

    $scope.$watch('$ctrl.selectedPublicGroup', selected => {
        if (selected && _.isObject(selected)) {
            subscribeToGroup(selected)
                .then(groupSubscriptions => vm.groupSubscriptions = groupSubscriptions)
                .then(() => notification.success(`Subscribed to: ${selected.name}`))
                .then(() => vm.selectedPublicGroup = null);

        }
    });
}

controller.$inject = [
    "ServiceBroker",
    "Notification",
    "$scope",
    "$state"
];


const component = {
    template,
    controller,
    bindings
};


export default {
    component,
    id: "waltzAppGroupListSection"
}