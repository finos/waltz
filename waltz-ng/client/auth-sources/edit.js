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


function calculateUsedTypes(allTypes = [], decorators = []) {
    const dataTypesById = _.keyBy(
        allTypes,
        "id");
    return _.chain(decorators)
        .map('decoratorEntity.id')
        .uniq()
        .map(id => dataTypesById[id])
        .value();
}


function calculateUnusedTypes(allTypes = [], used =[]) {
    const usedIds = _.map(used, 'id');
    return _.reject(
        allTypes,
        t => _.includes(usedIds, t.id));
}


function editController($state,
                        authSources,
                        flows,
                        flowDecorators,
                        orgUnits,
                        id,
                        authSourceStore,
                        notification,
                        dataTypes) {
    const vm = this;

    const authSourcesByCode = _.groupBy(authSources, 'dataType');
    const orgUnitsById = _.keyBy(orgUnits, 'id');
    const orgUnit = orgUnitsById[id];

    const usedDataTypes = calculateUsedTypes(dataTypes, flowDecorators);
    const unusedDataTypes = calculateUnusedTypes(dataTypes, usedDataTypes);

    const wizard = {
        dataType: null,
        app: null,
        rating: null
    };

    function getSupplyingApps(dataTypeCode) {
        const dataTypesByCode = _.keyBy(
            dataTypes,
            "code");
        const dataType = dataTypesByCode[dataTypeCode];
        const flowsById = _.keyBy(flows, 'id');

        return _.chain(flowDecorators)
            .filter(d => d.decoratorEntity.id == dataType.id)
            .map('dataFlowId')
            .uniq()
            .map(id => flowsById[id])
            .map(f => f.source)
            .uniqBy(app => app.id)
            .value();
    }

    function isDisabled() {
        return !(wizard.dataType && wizard.app && wizard.rating);
    }

    function selectExisting(authSource) {
        wizard.dataType = authSource.dataType;
        wizard.app = authSource.applicationReference;
        wizard.rating = authSource.rating;
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
                .then(
                    () => notification.success('Authoritative Sources updated'),
                    (e) => notification.error('Update failed, ' + e.data.message || e.statusText))
                .then(refresh);
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
                .then(
                    () => notification.success('Authoritative Sources added'),
                    (e) => notification.error('Add failed, ' + e.data.message || e.statusText))
                .then(refresh);
        }
    }


    function remove(authSource) {
        authSourceStore
            .remove(authSource.id)
            .then(refresh)
            .then(() => notification.warning('Authoritative Source removed'));
    }


    Object.assign(vm, {
        authSources,
        flows,
        orgUnits,
        id,
        orgUnit,
        orgUnitsById,
        authSourcesByCode,
        usedDataTypes,
        unusedDataTypes,

        getSupplyingApps,
        isDisabled,
        selectExisting,
        submit,
        remove,
        wizard
    });
}


editController.$inject = [
    '$state',
    'authSources',
    'flows',
    'flowDecorators',
    'orgUnits',
    'id',
    'AuthSourcesStore',
    'Notification',
    'dataTypes'
];


export default {
    template: require('./edit.html'),
    controller: editController,
    controllerAs: 'ctrl'
};
