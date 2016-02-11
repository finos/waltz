
/*
 *  This file is part of Waltz.
 *
 *  Waltz is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Waltz is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import _ from 'lodash';


const BINDINGS = {
    groupSubscriptions: '='
};


function removeUsedGroups(allGroups, existingSubscriptions) {
    const subscribedGroupIds = _.map(existingSubscriptions, 'appGroup.id');
    return _.reject(allGroups, g => _.contains(subscribedGroupIds, g.id));
}


function controller(appGroupStore, $scope) {

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


    vm.unsubscribe = (subscription) => {
        unsubscribeFromGroup(subscription)
            .then(groupSubscriptions => vm.groupSubscriptions = groupSubscriptions)
            .then(() =>  vm.editing = false);
    };


    $scope.$watch('ctrl.selectedPublicGroup', selected => {
        if (selected && _.isObject(selected)) {
            subscribeToGroup(selected)
                .then(groupSubscriptions => vm.groupSubscriptions = groupSubscriptions)
                .then(() => {
                    vm.editing = false;
                    vm.selectedPublicGroup = null;
                })
        }
    })
}

controller.$inject = [ 'AppGroupStore', '$scope', '$element' ];


export default () => {
    return {
        restrict: 'E',
        replace: true,
        template: require('./app-group-list-section.html'),
        scope: {},
        bindToController: BINDINGS,
        controllerAs: 'ctrl',
        controller
    };
};
