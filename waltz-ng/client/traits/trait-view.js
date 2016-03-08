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

function groupUsagesByEntityKind(allUsages = []) {
    return _.groupBy(allUsages, usage => usage.entityReference.kind)
}


function indexCapabilitiesById(capabilities = []) {
    return _.indexBy(capabilities, 'id')
}


function enrichCapabilityUsages(usages = [], capabilitiesById = {}) {
    console.log(usages, capabilitiesById)
    return _.map(usages, u => ({
        usage: u,
        capability: capabilitiesById[u.entityReference.id]
    }));
}


function requestCapabilityUsage(triatId, capabilityStore, traitUsageStore) {
    return [
        capabilityStore.findAll(),
        traitUsageStore.findByTraitId(triatId)
    ];
}


function controller(traitStore, traitUsageStore, capabilityStore, $stateParams, $q) {

    const id = $stateParams.id;
    const vm = this;

    vm.capabilitiesById = {};
    vm.trait = null;
    vm.usages = {};

    traitStore
        .getById(id)
        .then(trait =>
            vm.trait = trait);

    const promises = requestCapabilityUsage(id, capabilityStore, traitUsageStore);

    $q.all(promises)
        .then(([capabilities, allUsages]) => {
            const capabilitiesById = indexCapabilitiesById(capabilities);
            const usages = groupUsagesByEntityKind(allUsages);
            vm.capabilityUsages = enrichCapabilityUsages(usages['CAPABILITY'], capabilitiesById);
        });

    vm.lookupCapability = id =>
        vm.capabilitiesById[id];

    global.vm = vm;
}


controller.$inject = [
    'TraitStore',
    'TraitUsageStore',
    'CapabilityStore',
    '$stateParams',
    '$q'
];


export default ({
    template: require('./trait-view.html'),
    controller,
    controllerAs: 'ctrl'
})