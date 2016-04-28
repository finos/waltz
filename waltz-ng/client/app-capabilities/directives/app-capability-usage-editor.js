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

const BINDINGS = {
    usages: '=',
    capabilities: '=',
    capabilityTraits: '=?',
    traitUsages: '=?',
    add: '=',
    remove: '=',
    togglePrimary: '='
};


function controller($scope) {

    const vm = this;

    const watchExpressions = [
        'ctrl.usages',
        'ctrl.capabilities',
        'ctrl.traitUsages',
        'ctrl.capabilityTraits'
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

        vm.usedCapabilities = _.map(usages, u => ({ ...u, capability: capabilitiesById[u.capabilityId] }));

        vm.availableCapabilities = _.chain(capabilities)
            .reject(t => _.includes(usedCapabilityIds, t.id))
            .reject(t => _.includes(capabilityIdsToRemove, t.id))
            .value();

        vm.hasHiddenCapabilities = capabilityIdsToRemove.length > 0;

    });

    vm.addCapability = (c) => {
        console.log('c', c);
        vm.add(c).then(vm.selectedNode = null);
    }
}


controller.$inject = ['$scope'];


function directive() {
    return {
        restrict: 'E',
        replace: 'true',
        bindToController: BINDINGS,
        scope: {},
        template: require('./app-capability-usage-editor.html'),
        controller,
        controllerAs: 'ctrl'
    };
}

export default directive;