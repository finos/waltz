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

function groupUsagesByEntityKind(allUsages) {
    return _.groupBy(allUsages, usage => usage.entityReference.kind)
}


function indexCapabilitiesById(capabilities) {
    return _.indexBy(capabilities, 'id')
}


function controller(traitStore, traitUsageStore, capabilityStore, $stateParams) {

    const id = $stateParams.id;
    const vm = this;

    vm.capabilitiesById = {};
    vm.trait = null;
    vm.usages = {};

    capabilityStore.findAll()
        .then(capabilities =>
            vm.capabilitiesById = indexCapabilitiesById(capabilities));

    traitStore
        .getById(id)
        .then(trait =>
            vm.trait = trait);

    traitUsageStore
        .findByTraitId(id)
        .then(allUsages =>
            vm.usages = groupUsagesByEntityKind(allUsages));

    vm.lookupCapability = id =>
        vm.capabilitiesById[id];
}


controller.$inject = [
    'TraitStore',
    'TraitUsageStore',
    'CapabilityStore',
    '$stateParams'
];


export default ({
    template: require('./trait-view.html'),
    controller,
    controllerAs: 'ctrl'
})