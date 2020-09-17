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
import {reduceToSelectedNodesOnly} from "../../../common/hierarchy-utils";
import {mkRef} from "../../../common/entity-utils";


const bindings = {
    parentEntityRef: "<",
    onDirty: "<",
    onRegisterSave: "<",
    parentFlow:"<?"
};


const initialState = {
    dataTypes: [],
    allDataTypes: [],
    checkedItemIds: [],
    originalSelectedItemIds: [],
    expandedItemIds: [],
    disablePredicate: null,
    suggestedDataTypes: [],
    showAllDataTypes: false,
    unableToBeRemoved: [],
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


function controller(serviceBroker, notification) {
    const vm = initialiseData(this, initialState);

    const loadLogicalOnlyDatatypes = () => {
        if(vm.parentEntityRef.kind === "LOGICAL_DATA_FLOW"){
            serviceBroker
                .loadViewData(CORE_API.DataTypeDecoratorStore.findDecoratorsExclusiveToEntity,
                    [vm.parentEntityRef],
                    {force: true})
                .then(r => vm.logicalOnlyDatatypes = _.map(r.data, d => d.dataTypeId))
        }
    };

    const postLoadActions = () => {
        const selectedDataTypeIds = mkSelectedTypeIds(vm.dataTypes);
        vm.checkedItemIds = selectedDataTypeIds;
        vm.originalSelectedItemIds = selectedDataTypeIds;
        vm.expandedItemIds = selectedDataTypeIds;

        const suggestedAndSelectedTypes = _.concat(selectedDataTypeIds, _.map(vm.suggestedDataTypes, d => d.id));
        vm.allDataTypes = enrichDataTypes(vm.allDataTypes, vm.checkedItemIds);
        vm.allDataTypesById = _.keyBy(vm.allDataTypes, "id");

        vm.readOnlyDatatypeIdsForParent = _.chain(vm.dataTypes)
            .filter(d => d.isReadonly)
            .map(d => d.dataTypeId)
            .value();

        vm.visibleDataTypes = vm.showAllDataTypes
            ? vm.allDataTypes
            :reduceToSelectedNodesOnly(vm.allDataTypes, suggestedAndSelectedTypes);

        loadLogicalOnlyDatatypes();
    };

    function getDatatypesUnableToBeRemoved(decoratorUpdateCommand) {
        serviceBroker
            .loadViewData(CORE_API.DataTypeDecoratorStore.getRemovableDatatypes,
                [mkRef('LOGICAL_DATA_FLOW', vm.parentFlow.logicalFlowId), decoratorUpdateCommand.removedDataTypeIds])
            .then(r => {
                vm.datatypesNotRemoved = _.map(
                    _.difference(decoratorUpdateCommand.removedDataTypeIds, r.data),
                    d => vm.allDataTypesById[d].name);
                vm.notRemovedString = _.join(vm.datatypesNotRemoved, ", ");
            });
    }

    const doSave = () => {

        const decoratorUpdateCommand = mkDataTypeUpdateCommand(
            vm.parentEntityRef,
            vm.checkedItemIds,
            vm.originalSelectedItemIds);

        if (_.get(vm.parentFlow, 'kind', null) === 'PHYSICAL_FLOW'){
            getDatatypesUnableToBeRemoved(decoratorUpdateCommand);
        }

        return serviceBroker
            .execute(
                CORE_API.DataTypeDecoratorStore.save,
                [ vm.parentEntityRef, decoratorUpdateCommand ])
            .then(r => {
                if(!_.isEmpty(vm.datatypesNotRemoved)){
                    notification.error(
                        "These datatypes were not removed from the logical flow as they are shared with other physical specs: "
                        + vm.notRemovedString)
                }
            });
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
                dataFlowId: d.dataFlowId,
                isReadonly: d.isReadonly
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

    vm.isReadonly = (node) => {
        return _.includes(vm.readOnlyDatatypeIdsForParent, node.id);
    };

    vm.isReadonlyPredicate = (node) => {
        return vm.isReadonly(node) || vm.isRestrictedBySpecs(node);
    };

    vm.isRestrictedBySpecs = (node) => {
        const isLogical = vm.parentEntityRef.kind === 'LOGICAL_DATA_FLOW';
        const originallyChecked = _.includes(vm.originalSelectedItemIds, node.id);
        const logicalDatatypeOnly = !_.includes(vm.logicalOnlyDatatypes, node.id);
        return isLogical && originallyChecked && logicalDatatypeOnly;
    };

    const determineMessage = () => {
        vm.showAllMessage = (vm.showAllDataTypes)
            ? "Show suggested data types"
            : "Show all data types";
    };

    vm.toggleShowAll = () => {
        vm.showAllDataTypes = !vm.showAllDataTypes;
        postLoadActions();
        determineMessage();
    };

    // -- LIFECYCLE

    const loadSuggestedDatatypes = () => {
        if (vm.parentFlow) {
            return serviceBroker.loadViewData(CORE_API.DataTypeDecoratorStore.findSuggestedByEntityRef, [vm.parentFlow])
                .then(r => vm.suggestedDataTypes = _.filter(r.data, dt => !dt.unknown))
                .then(() => vm.showAllDataTypes = _.isEmpty(vm.suggestedDataTypes));
        } else {
            vm.showAllDataTypes = true;
        }
    };

    vm.$onInit = () => {
        vm.onDirty(false);
        vm.onRegisterSave(vm.save);
        determineMessage();

        serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(result => {
                vm.allDataTypes = result.data;
                vm.unknownDataType = _.find(vm.allDataTypes, dt => dt.unknown);
            });

        loadDataTypes()
            .then(() => loadSuggestedDatatypes())
            .then(() => postLoadActions());
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
    "ServiceBroker",
    "Notification"
];


export default {
    component: {
        template,
        bindings,
        controller
    },
    id: "waltzDataTypeUsageSelector"
};

