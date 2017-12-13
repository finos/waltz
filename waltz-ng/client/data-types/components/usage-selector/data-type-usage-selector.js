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
    specDataTypes: [],
    allDataTypes: [],
    checkedItemIds: [],
    originalSelectedItemIds: [],
    expandedItemIds: [],
    onDirty: (d) => console.log('dtus:onDirty - default impl', d),
    onRegisterSave: (f) => console.log('dtus:onRegisterSave - default impl', f)
};


function mkSelectedTypeIds(specDataTypes = []) {
    return _.map(specDataTypes, 'dataTypeId');
}


function mkUpdateCommand(specificationId, selectedIds = [], originalIds = []) {
    const addedDataTypeIds = _.difference(selectedIds, originalIds);
    const removedDataTypeIds = _.difference(originalIds, selectedIds);

    return {
        specificationId,
        addedDataTypeIds,
        removedDataTypeIds
    };
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const postLoadActions = () => {
        const selectedDataTypeIds = mkSelectedTypeIds(vm.specDataTypes);
        vm.checkedItemIds = selectedDataTypeIds;
        vm.originalSelectedItemIds = selectedDataTypeIds;
        vm.expandedItemIds = selectedDataTypeIds;
    };

    const loadSpecDataTypes = (force = false) => {
        const selectorOptions = {
            entityReference: vm.parentEntityRef,
            scope: 'EXACT'
        };
        return serviceBroker
            .loadViewData(
                CORE_API.PhysicalSpecDataTypeStore.findBySpecificationSelector,
                [ selectorOptions ],
                { force })
            .then(result => vm.specDataTypes = result.data);
    };

    serviceBroker
        .loadAppData(CORE_API.DataTypeStore.findAll)
        .then(result => vm.allDataTypes = result.data);


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
        vm.onDirty(true);
        vm.checkedItemIds = _.union(vm.checkedItemIds, [id])
    };

    vm.save = () => {
        const updateCommand = mkUpdateCommand(
            vm.parentEntityRef.id,
            vm.checkedItemIds,
            vm.originalSelectedItemIds);

        return serviceBroker
            .execute(CORE_API.PhysicalSpecDataTypeStore.save, [vm.parentEntityRef.id, updateCommand])
            .then(result => loadSpecDataTypes(true))
            .then(() => {
                postLoadActions();
                vm.onDirty(false);
            });
    };

    vm.$onInit = () => {
        vm.onDirty(false);
        vm.onRegisterSave(vm.save);

        loadSpecDataTypes()
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

