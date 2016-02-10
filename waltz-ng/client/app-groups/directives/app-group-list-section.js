
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
    groups: '='
};


function removeUsedGroups(allGroups, usedGroups) {
    const usedGroupIds = _.map(usedGroups, 'id');
    return _.reject(allGroups, g => _.contains(usedGroupIds, g.id));
}


function controller(appGroupStore, $scope) {


    function loadPublicGroups() {
        appGroupStore.findPublicGroups()
            .then(publicGroups => removeUsedGroups(publicGroups, vm.groups))
            .then(availableGroups => vm.availableGroups = availableGroups);
    }


    function subscribeToGroup(group) {
        return appGroupStore.subscribe(group.id)
            .then(() => vm.groups.push(group));
    }


    const vm = this;
    vm.availableGroups = [];

    vm.showAdd = false;

    vm.toggleShowAdd = () => {
        vm.showAdd = ! vm.showAdd;

        if (vm.showAdd) {
            loadPublicGroups();
        }
    }


    $scope.$watch('ctrl.selectedPublicGroup', selected => {
        console.log(selected);
        if (selected && _.isObject(selected)) {
            subscribeToGroup(selected)
                .then(() => vm.showAdd = false);
        }
    })
}

controller.$inject = [ 'AppGroupStore', '$scope' ];


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
