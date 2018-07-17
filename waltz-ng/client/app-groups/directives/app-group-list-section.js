
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
