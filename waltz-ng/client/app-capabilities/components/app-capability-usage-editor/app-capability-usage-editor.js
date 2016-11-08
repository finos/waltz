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

import _ from "lodash";


const bindings = {
    usages: '<',
    capabilities: '<',
    capabilityTraits: '<',
    traitUsages: '<',
    add: '<',
    remove: '<',
    update: '<',
    togglePrimary: '<'
};


const template = require('./app-capability-usage-editor.html');

const initialState = {
    mode: 'NONE' // NONE, EDIT, ADD
}

function controller($scope) {

    const vm = this;

    const watchExpressions = [
        '$ctrl.usages',
        '$ctrl.capabilities',
        '$ctrl.traitUsages',
        '$ctrl.capabilityTraits'
    ];

    $scope.$watchGroup(watchExpressions, ([usages, capabilities, traitUsages = [], capabilityTraits = []]) => {
        if (! usages || ! capabilities) { return; }

        const capabilitiesById = _.keyBy(capabilities, 'id');
        const usedCapabilityIds = _.map(usages, usage => usage.capabilityId);

        const exhibitedTraits = _.map(traitUsages, 'traitId');

        const capabilityIdsToRemove = _.chain(capabilityTraits)
            .filter({ relationship: 'REQUIRES' })
            .reject(ct => _.includes(exhibitedTraits, ct.traitId))
            .map('entityReference.id')
            .value();

        vm.usedCapabilities = _.map(usages, u => ({ ...u, capability: capabilitiesById[u.capabilityId] , rating: 'Z'} ));

        vm.availableCapabilities = _.chain(capabilities)
            .reject(t => _.includes(usedCapabilityIds, t.id))
            .reject(t => _.includes(capabilityIdsToRemove, t.id))
            .value();

        vm.hasHiddenCapabilities = capabilityIdsToRemove.length > 0;

    });


    vm.addCapability = (c) => {
        vm.add(c.capability).then(vm.selected = null);
        console.log('TODO: add rating for cap: ', c);
        vm.changeMode('NONE');
    };


    vm.updateRating = (c) => {
        console.log("TODO: update rating for capability: ", c)
        vm.changeMode('NONE');
    };


    vm.changeMode = (mode) => {
        vm.mode = mode;
    }

    vm.edit = (usage) => {
        if(vm.selected) {
            vm.selected.editMode = false;
        }
        vm.changeMode('EDIT');
        vm.selected = usage;
        vm.selected.editMode = true;
    };


    vm.cancelEdit = () => {
        vm.changeMode('NONE');
        if(vm.selected) {
            vm.selected.editMode = false;
        }
    };


    vm.addNew = () => {
        if(vm.selected) {
            vm.selected.editMode = false;
            vm.selected = null;
        }
        vm.changeMode('ADD');
    };

}



controller.$inject = ['$scope'];


const component = {
    bindings,
    template,
    controller
}


export default component;