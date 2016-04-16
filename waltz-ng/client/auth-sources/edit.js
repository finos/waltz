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
import _ from "lodash";


function editController(authSources,
                        flows,
                        orgUnits,
                        id,
                        authSourceStore,
                        $state,
                        displayNameService) {

    const dataTypes = displayNameService.toOptions('dataType');

    const authSourcesByType = _.groupBy(authSources, 'dataType');
    const orgUnitsById = _.keyBy(orgUnits, 'id');
    const orgUnit = orgUnitsById[id];
    const usedTypes = _.chain(flows).map('dataType').uniq().value();
    const unusedTypes = _.chain(dataTypes).map('code').without(...usedTypes).value();

    const wizard = {
        dataType: null,
        app: null,
        rating: null
    };

    function getSupplyingApps(dataType) {
        const apps = _.chain(flows)
            .filter(f => f.dataType === dataType)
            .filter(f => f.source.kind === 'APPLICATION')
            .map('source')
            .uniq('id')
            .value();

        return apps;
    }

    function isDisabled() {
        return !(wizard.dataType && wizard.app && wizard.rating);
    }

    function selectExisting(authSource) {
        wizard.dataType = authSource.dataType;
        wizard.app = authSource.applicationReference;
        wizard.rating = authSource.rating;
    }

    function resetWizard() {
        wizard.dataType = null;
        wizard.app = null;
        wizard.rating = null;
    }


    function refresh() {
        $state.reload();
    }


    function submit() {
        const existingAuthSource = _.find(authSources, a =>
            a.applicationReference.id === wizard.app.id &&
            a.dataType === wizard.dataType );

        if (existingAuthSource) {
            authSourceStore
                .update(existingAuthSource.id, wizard.rating)
                .then(() => existingAuthSource.rating = wizard.rating)
                .then(() => resetWizard());
        } else {
            const insertRequest = {
                kind: 'ORG_UNIT',
                id,
                dataType: wizard.dataType,
                appId: wizard.app.id,
                rating: wizard.rating
            };
            authSourceStore
                .insert(insertRequest)
                .then(refresh);
        }
    }


    function remove(authSource) {
        authSourceStore
            .remove(authSource.id)
            .then(refresh);
    }


    const vm = this;
    Object.assign(vm, {
        authSources,
        flows,
        orgUnits,
        id,
        orgUnit,
        orgUnitsById,
        authSourcesByType,
        usedTypes,
        unusedTypes,

        getSupplyingApps,
        isDisabled,
        selectExisting,
        submit,
        remove,
        wizard
    });
}


editController.$inject = [
    'authSources',
    'flows',
    'orgUnits',
    'id',
    'AuthSourcesStore',
    '$state',
    'WaltzDisplayNameService'
];


export default {
    template: require('./edit.html'),
    controller: editController,
    controllerAs: 'ctrl'
};
