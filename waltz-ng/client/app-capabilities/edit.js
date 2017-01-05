/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
