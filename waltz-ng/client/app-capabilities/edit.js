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

const controller = function(appCapabilityStore,
                            appStore,
                            capabilityStore,
                            notification,
                            $stateParams,
                            $state,
                            $q) {

    const vm = this;
    const id = $stateParams.id;


    const refresh = () => appCapabilityStore
        .findCapabilitiesByApplicationId(id)
        .then(usages =>  vm.usages = usages);


    const boot = () => {
        const promises = [
            appStore.getById(id),
            capabilityStore.findAll(),
        ];

        $q.all(promises)
            .then(([app, capabilities ]) => {
                vm.capabilities = capabilities;
                vm.application = app;
            });

        refresh();
    };


    // -- INTERACT ---

    vm.onSave = (cmd) => appCapabilityStore
        .save(vm.application.id, cmd)
        .then(() => notification.success(`Function ${ cmd.isNew ? 'Added' : 'Updated' }`))
        .then(refresh);

    vm.onRemove = (c) => appCapabilityStore
        .removeCapability(id, c.id)
        .then(() => notification.success(`Removed function: ${c.name}`))
        .then(refresh);


    // -- BOOT ---

    boot();

};


controller.$inject = [
    'AppCapabilityStore',
    'ApplicationStore',
    'CapabilityStore',
    'Notification',
    '$stateParams',
    '$state',
    '$q'
];


export default {
    template: require('./edit.html'),
    controller,
    controllerAs: 'ctrl'
};
