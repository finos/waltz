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
import template from './app-group-list-section.html';

const BINDINGS = {
    groupSubscriptions: '='
};


function removeUsedGroups(allGroups, existingSubscriptions) {
    const subscribedGroupIds = _.map(existingSubscriptions, 'appGroup.id');
    return _.reject(allGroups, g => _.includes(subscribedGroupIds, g.id));
}


function controller(appGroupStore, notification, $scope, $state) {

    function loadPublicGroups() {
        appGroupStore.findPublicGroups()
            .then(publicGroups => removeUsedGroups(publicGroups, vm.groupSubscriptions))
            .then(availableGroups => vm.availableGroups = availableGroups);
    }


    function subscribeToGroup(group) {
        return appGroupStore.subscribe(group.id);
    }


    function unsubscribeFromGroup(subscription) {
        return appGroupStore.unsubscribe(subscription.appGroup.id);
    }


    const vm = this;
    vm.availableGroups = [];

    vm.editing = false;

    vm.toggleEditing= () => {
        vm.editing = ! vm.editing;

        if (vm.editing) {
            loadPublicGroups();
        }
    };


    vm.createNewGroup = () => {
        appGroupStore.createNewGroup()
            .then(id => {
                notification.success('New group created');
                $state.go('main.app-group.edit', { id })
            });
    };


    vm.unsubscribe = (subscription) => {
        unsubscribeFromGroup(subscription)
            .then(groupSubscriptions => vm.groupSubscriptions = groupSubscriptions)
            .then(() => notification.warning('Unsubscribed from: ' + subscription.appGroup.name));
    };


    vm.deleteGroup = (group) => {
        if (! confirm("Really delete this group ? \n " + group.appGroup.name)) return;

        appGroupStore.deleteGroup(group.appGroup.id)
            .then(groupSubscriptions => vm.groupSubscriptions = groupSubscriptions)
            .then(() => notification.warning('Deleted group: ' + group.appGroup.name));

    };


    $scope.$watch('ctrl.selectedPublicGroup', selected => {
        if (selected && _.isObject(selected)) {
            subscribeToGroup(selected)
                .then(groupSubscriptions => vm.groupSubscriptions = groupSubscriptions)
                .then(() => notification.success('Subscribed to: ' + selected.name))
                .then(() => vm.selectedPublicGroup = null);

        }
    });
}

controller.$inject = [
    'AppGroupStore',
    'Notification',
    '$scope',
    '$state'
];


export default () => {
    return {
        restrict: 'E',
        replace: true,
        template,
        scope: {},
        bindToController: BINDINGS,
        controllerAs: 'ctrl',
        controller
    };
};
