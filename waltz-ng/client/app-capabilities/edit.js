/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */

import _ from 'lodash';


const controller = function(appCapabilityStore,
                            appStore,
                            capabilityStore,
                            notification,
                            traitStore,
                            traitUsageStore,
                            $stateParams,
                            $state,
                            $q) {

    const vm = this;
    const id = $stateParams.id;

    const promises = [
        appStore.getById(id),
        appCapabilityStore.findCapabilitiesByApplicationId(id),
        capabilityStore.findAll(),
        traitStore.findApplicationDeclarableTraits(),
        traitUsageStore.findByEntityReference('APPLICATION', id)
    ];


    $q.all(promises)
        .then(([app, capabilityUsages, allCapabilities, allTraits, traitUsage]) => {
            vm.traits = {
                all: allTraits,
                usages: traitUsage
            };

            vm.capabilities = {
                all: allCapabilities,
                usages: capabilityUsages
            };

            vm.application = app;
        });


    vm.togglePrimary = (c, primary) => appCapabilityStore
        .setIsPrimary(id, c.id, primary)
        .then(() => notification.success(`${c.name} ${primary ? ' not ' : ''}  marked as primary`))
        .then(() => appCapabilityStore.findCapabilitiesByApplicationId(id))
        .then(usages => vm.capabilities.usages = usages);

    vm.addCapability = (c) => appCapabilityStore
        .addCapability(id, c.id)
        .then(() => notification.success(`Added capability: ${c.name}`))
        .then(() => appCapabilityStore.findCapabilitiesByApplicationId(id))
        .then(usages => vm.capabilities.usages = usages);

    vm.removeCapability = (c) => appCapabilityStore
        .removeCapability(id, c.id)
        .then(() => notification.success(`Removed capability: ${c.name}`))
        .then(() => appCapabilityStore.findCapabilitiesByApplicationId(id))
        .then(usages => vm.capabilities.usages = usages);

    vm.addTrait = (t) => traitUsageStore
        .addUsage({ kind: 'APPLICATION', id}, t.id)
        .then(usages => vm.traits.usages = usages)
        .then(() => notification.success('Trait registered'));

    vm.removeTrait = (t) => traitUsageStore
        .removeUsage({ kind: 'APPLICATION', id}, t.id)
        .then(usages => vm.traits.usages = usages)
        .then(() => notification.warning('Trait registration removed'));

};

controller.$inject = [
    'AppCapabilityStore',
    'ApplicationStore',
    'CapabilityStore',
    'Notification',
    'TraitStore',
    'TraitUsageStore',
    '$stateParams',
    '$state',
    '$q'
];


export default {
    template: require('./edit.html'),
    controller,
    controllerAs: 'ctrl'
};
