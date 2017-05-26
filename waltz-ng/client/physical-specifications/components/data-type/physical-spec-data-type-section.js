import _ from "lodash";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";

import template from "./physical-spec-data-type-section.html";


const bindings = {
    parentEntityRef: '<'
};


const initialState = {
    specDataTypes: [],
    allDataTypes: [],
    checkedItemIds: [],
    originalSelectedItemIds: [],
    expandedItemIds: [],
    editing: false
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


function controller(notification, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const postLoadActions = () => {
        const selectedDataTypeIds = mkSelectedTypeIds(vm.specDataTypes);
        vm.checkedItemIds = selectedDataTypeIds;
        vm.originalSelectedItemIds = selectedDataTypeIds;
        vm.expandedItemIds = selectedDataTypeIds;
    };

    const loadSpecDataTypes = (force) => {
        const selectorOptions = {
            entityReference: vm.parentEntityRef,
            scope: 'EXACT'
        };
        return serviceBroker
            .loadViewData(
                CORE_API.PhysicalSpecDataTypeStore.findBySpecificationSelector,
                [selectorOptions],
                {force})
            .then(result => vm.specDataTypes = result.data);
    };

    serviceBroker
        .loadAppData(CORE_API.DataTypeStore.findAll, [])
        .then(result => vm.allDataTypes = result.data);


    vm.typeChecked = (id) => {
        vm.checkedItemIds = _.union(vm.checkedItemIds, [id])
    };

    vm.toggleTypeChecked = (id) => {
        _.some(vm.checkedItemIds, x => x === id)
            ? vm.typeUnchecked(id)
            : vm.typeChecked(id);
    };

    vm.typeUnchecked = (id) => {
        vm.checkedItemIds = _.without(vm.checkedItemIds, id);
    };

    vm.startEdit = () => {
        vm.editing = true;
    };

    vm.save = () => {
        const updateCommand = mkUpdateCommand(
            vm.parentEntityRef.id,
            vm.checkedItemIds,
            vm.originalSelectedItemIds);

        serviceBroker
            .execute(CORE_API.PhysicalSpecDataTypeStore.save, [vm.parentEntityRef.id, updateCommand])
            .then(result => loadSpecDataTypes(true))
            .then(() => {
                postLoadActions();
                vm.editing = false;
                notification.success('Data types saved successfully');
            });
    };

    vm.cancel = () => {
        vm.editing = false;
    };

    // load first time
    loadSpecDataTypes()
        .then(postLoadActions);
}


controller.$inject = [
    'Notification',
    'ServiceBroker'
];


export default {
    component: {
        template,
        bindings,
        controller
    },
    id: 'waltzPhysicalSpecDataTypeSection'
};

