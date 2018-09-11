/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";

import template from "./data-type-usage-selector.html";


const bindings = {
    parentEntityRef: '<',
    onDirty: '<',
    onRegisterSave: '<'
};


const initialState = {
    dataTypes: [],
    allDataTypes: [],
    checkedItemIds: [],
    originalSelectedItemIds: [],
    expandedItemIds: [],
    onDirty: (d) => console.log('dtus:onDirty - default impl', d),
    onRegisterSave: (f) => console.log('dtus:onRegisterSave - default impl', f)
};


function mkSelectedTypeIds(dataTypes = []) {
    return _.map(dataTypes, 'dataTypeId');
}


function mkSpecDataTypeUpdateCommand(specificationId, selectedIds = [], originalIds = []) {
    const addedDataTypeIds = _.difference(selectedIds, originalIds);
    const removedDataTypeIds = _.difference(originalIds, selectedIds);

    return {
        specificationId,
        addedDataTypeIds,
        removedDataTypeIds
    };
}


function mkFlowDataTypeDecoratorsUpdateCommand(flowId, selectedIds = [], originalIds = []) {
    const addedDecorators = _.chain(selectedIds)
        .difference(originalIds)
        .map(id => ({kind: 'DATA_TYPE', id}))
        .value();

    const removedDecorators = _.chain(originalIds)
        .difference(selectedIds)
        .map(id => ({kind: 'DATA_TYPE', id}))
        .value();

    return {
        flowId,
        addedDecorators,
        removedDecorators
    };
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const postLoadActions = () => {
        const selectedDataTypeIds = mkSelectedTypeIds(vm.dataTypes);
        vm.checkedItemIds = selectedDataTypeIds;
        vm.originalSelectedItemIds = selectedDataTypeIds;
        vm.expandedItemIds = selectedDataTypeIds;
    };

    const loadDataTypes = (force = false) => {
        const selectorOptions = {
            entityReference: vm.parentEntityRef,
            scope: 'EXACT'
        };
        const promise = vm.parentEntityRef.kind == 'PHYSICAL_SPECIFICATION'
            ? serviceBroker
                .loadViewData(
                    CORE_API.PhysicalSpecDataTypeStore.findBySpecificationSelector,
                    [ selectorOptions ],
                    { force })
                .then(r => r.data)
            : serviceBroker
                .loadViewData(
                    CORE_API.LogicalFlowDecoratorStore.findByFlowIdsAndKind,
                    [ [vm.parentEntityRef.id] ],
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

    serviceBroker
        .loadAppData(CORE_API.DataTypeStore.findAll)
        .then(result => {
            vm.allDataTypes = result.data;
            vm.allDataTypesById = _.keyBy(result.data, 'id');
        });


    vm.toggleTypeChecked = (id) => {
        _.some(vm.checkedItemIds, x => x === id)
            ? vm.typeUnchecked(id)
            : vm.typeChecked(id);
    };

    vm.typeUnchecked = (id) => {
        vm.onDirty(true);
        vm.checkedItemIds = _.without(vm.checkedItemIds, id);
    };

    vm.typeChecked = (id) => {
        // deselect any parents that are non-concrete
        let dt = vm.allDataTypesById[id];
        while (dt) {
            const parent = vm.allDataTypesById[dt.parentId];
            if (_.get(parent, 'concrete', true) === false) {
                vm.typeUnchecked(parent.id);
            }
            dt = parent;
        }
        vm.onDirty(true);
        vm.checkedItemIds = _.union(vm.checkedItemIds, [id])
    };

    vm.save = () => {
        let promise = null;
        if(vm.parentEntityRef.kind === 'PHYSICAL_SPECIFICATION') {
            const updateCommand = mkSpecDataTypeUpdateCommand(
                vm.parentEntityRef.id,
                vm.checkedItemIds,
                vm.originalSelectedItemIds);

            promise = serviceBroker
                .execute(CORE_API.PhysicalSpecDataTypeStore.save, [vm.parentEntityRef.id, updateCommand]);
        } else if(vm.parentEntityRef.kind === 'LOGICAL_DATA_FLOW') {
            const updateCommand = mkFlowDataTypeDecoratorsUpdateCommand(
                vm.parentEntityRef.id,
                vm.checkedItemIds,
                vm.originalSelectedItemIds);

            promise = serviceBroker
                .execute(CORE_API.LogicalFlowDecoratorStore.updateDecorators, [updateCommand]);
        }
        return promise
            .then(result => loadDataTypes(true))
            .then(() => {
                postLoadActions();
                vm.onDirty(false);
            });
    };

    vm.$onInit = () => {
        vm.onDirty(false);
        vm.onRegisterSave(vm.save);

        loadDataTypes()
            .then(postLoadActions);
    };
}


controller.$inject = [
    'ServiceBroker'
];


export default {
    component: {
        template,
        bindings,
        controller
    },
    id: 'waltzDataTypeUsageSelector'
};

