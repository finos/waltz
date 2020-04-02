/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

import _ from "lodash";
import {initialiseData, notEmpty} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";

import template from "./data-type-usage-selector.html";
import {enrichDataTypes} from "../../data-type-utils";


const bindings = {
    parentEntityRef: "<",
    onDirty: "<",
    onRegisterSave: "<"
};


const initialState = {
    dataTypes: [],
    allDataTypes: [],
    checkedItemIds: [],
    originalSelectedItemIds: [],
    expandedItemIds: [],
    disablePredicate: null,
    onDirty: (d) => console.log("dtus:onDirty - default impl", d),
    onRegisterSave: (f) => console.log("dtus:onRegisterSave - default impl", f)
};


function mkSelectedTypeIds(dataTypes = []) {
    return _.map(dataTypes, "dataTypeId");
}


function mkDataTypeUpdateCommand(entityReference, selectedIds = [], originalIds = []) {
    const addedDataTypeIds = _.difference(selectedIds, originalIds);
    const removedDataTypeIds = _.difference(originalIds, selectedIds);

    return {
        entityReference,
        addedDataTypeIds,
        removedDataTypeIds
    };
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const postLoadActions = () => {
        const selectedDataTypeIds = mkSelectedTypeIds(vm.dataTypes);
        vm.checkedItemIds = selectedDataTypeIds;
        vm.originalSelectedItemIds = selectedDataTypeIds;
        vm.expandedItemIds = selectedDataTypeIds;
        vm.allDataTypes = enrichDataTypes(vm.allDataTypes, vm.checkedItemIds);
        vm.allDataTypesById = _.keyBy(vm.allDataTypes, "id");
    };

    const doSave = () => {
        const decoratorUpdateCommand = mkDataTypeUpdateCommand(
            vm.parentEntityRef,
            vm.checkedItemIds,
            vm.originalSelectedItemIds);
        return serviceBroker
            .execute(
                CORE_API.DataTypeDecoratorStore.save,
                [ vm.parentEntityRef, decoratorUpdateCommand ]);
    };

    const loadDataTypes = (force = false) => {

        const promise = serviceBroker
            .loadViewData(
                CORE_API.DataTypeDecoratorStore.findByEntityReference,
                [ vm.parentEntityRef ],
                { force })
            .then(r => r.data)
            .then(decorators => _.map(decorators, d => ({
                lastUpdatedAt: d.lastUpdatedAt,
                lastUpdatedBy: d.lastUpdatedBy,
                provenance: d.provenance,
                dataTypeId: d.decoratorEntity.id,
                dataFlowId: d.dataFlowId
            })));

        return promise.then(result => vm.dataTypes = result);
    };

    const anySelected = () => {
        return notEmpty(vm.checkedItemIds);
    };

    const hasAnyChanges = () => {
        return !_.isEqual(vm.checkedItemIds.sort(), vm.originalSelectedItemIds.sort());
    };


    // -- INTERACT

    vm.typeUnchecked = (id) => {
        vm.checkedItemIds = _.without(vm.checkedItemIds, id);
        vm.onDirty(hasAnyChanges() && anySelected());
        //set disable flag of selected non concrete to true
        if(!vm.allDataTypesById[id].concrete) {
            _.find(vm.allDataTypes, { id: id}).disable = true;
            vm.allDataTypesById[id].disable = true;
        }
    };

    vm.typeChecked = (id) => {
        // deselect any parents that are non-concrete
        let dt = vm.allDataTypesById[id];
        while (dt) {
            const parent = vm.allDataTypesById[dt.parentId];
            if (_.get(parent, "concrete", true) === false) {
                vm.typeUnchecked(parent.id);
            }
            dt = parent;
        }

        vm.checkedItemIds = _
            .chain(vm.checkedItemIds)
            .reject(dtId => vm.unknownDataType ? dtId === vm.unknownDataType.id : false)
            .union([id])
            .value();

        vm.onDirty(hasAnyChanges());
    };

    vm.save = () => {
        return doSave()
            .then(() => loadDataTypes(true))
            .then(() => {
                postLoadActions();
                vm.onDirty(false);
            });
    };

    vm.disablePredicate = (node) => {
        return !node.concrete;
    };

    // -- LIFECYCLE

    vm.$onInit = () => {
        vm.onDirty(false);
        vm.onRegisterSave(vm.save);

        serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(result => {
                vm.allDataTypes = result.data;
                vm.unknownDataType = _.find(vm.allDataTypes, dt => dt.unknown);
            });

        loadDataTypes()
            .then(postLoadActions);
    };

    vm.$onChanges = () => {
        loadDataTypes()
            .then(() => {
                postLoadActions();
                vm.onDirty(false);
            });
    };

}


controller.$inject = [
    "ServiceBroker"
];


export default {
    component: {
        template,
        bindings,
        controller
    },
    id: "waltzDataTypeUsageSelector"
};

