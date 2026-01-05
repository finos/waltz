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
import {loadUsageData} from "../../data-type-utils";
import {reduceToSelectedNodesOnly} from "../../../common/hierarchy-utils";
import {proposeDataFlowRemoteStore} from "../../../svelte-stores/propose-data-flow-remote-store";
import pageInfo from "../../../svelte-stores/page-navigation-store";
import {PROPOSAL_TYPES} from "../../../common/constants";
import {
    duplicateProposeFlowMessage,
    existingProposeFlowId
} from "../../../data-flow/components/svelte/propose-data-flow/propose-data-flow-store";
import {handleProposalValidation} from "../../../common/utils/proposalValidation";

const bindings = {
    parentEntityRef: "<",
    onDirty: "<?",
    onRegisterSave: "<?",
    onRegisterSavePropose: "<?",
    onSelect: "<?"
};


const initialState = {
    dataTypes: [],
    rawDataTypes: [],
    checkedItemIds: [],
    originalSelectedItemIds: [],
    expandedItemIds: [],
    disablePredicate: null,
    suggestedDataTypes: [],
    showAllDataTypes: false,
    onDirty: (d) => console.log("dtus:onDirty - default impl", d),
    onSelect: (d) => console.log("dtus:onSelect - default impl", d),
    onRegisterSave: (f) => console.log("dtus:onRegisterSave - default impl", f),
    onRegisterSavePropose: (f) => console.log("dtus:onRegisterSavePropose - default impl", f)
};


function goToWorkflow(proposedFlowId) {
    // Simple navigation using window.location
    pageInfo.set({
        state: "main.proposed-flow.view",
        params: {
            id: proposedFlowId
        }
    })
    existingProposeFlowId.set(null)
    duplicateProposeFlowMessage.set(null)
}


function mkSelectedTypeIds(usage = []) {
    return _.map(usage, d => d.dataTypeId);
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


function enrichDataTypeWithUsage(dataType, usageById = {}) {
    const usage = _.get(usageById, dataType.id, null);
    return {
        id: dataType.id,
        parentId: dataType.parentId,
        dataType,
        usage
    };
}


function enrichDataTypes(dataTypes = [], usageCharacteristics = []) {
    const usageById = _.keyBy(usageCharacteristics, d => d.dataTypeId);
    return _.map(dataTypes, dt => enrichDataTypeWithUsage(dt, usageById));
}


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);


    const loadSuggestedDataTypes = () => {
        return serviceBroker
            .loadViewData(
                CORE_API.DataTypeStore.findSuggestedByEntityRef,
                [vm.parentEntityRef],
                {force: true})
            .then(r => _.filter(r.data, dt => !dt.unknown));
    };


    const postLoadActions = (used = [], suggestions = []) => {

        vm.enrichedDataTypes = enrichDataTypes(vm.rawDataTypes, used);

        const selectedDataTypeIds = mkSelectedTypeIds(used);
        vm.checkedItemIds = selectedDataTypeIds;
        vm.originalSelectedItemIds = selectedDataTypeIds;
        vm.expandedItemIds = selectedDataTypeIds;

        const suggestedAndSelectedTypes = _.concat(selectedDataTypeIds, _.map(suggestions, d => d.id));
        vm.enrichedDataTypesById = _.keyBy(vm.enrichedDataTypes, "id");

        vm.visibleDataTypes = vm.showAllDataTypes
            ? vm.enrichedDataTypes
            : reduceToSelectedNodesOnly(vm.enrichedDataTypes, suggestedAndSelectedTypes);

        return vm.enrichedDataTypes;
    };

    const doSave = () => {

        const decoratorUpdateCommand = mkDataTypeUpdateCommand(
            vm.parentEntityRef,
            vm.checkedItemIds,
            vm.originalSelectedItemIds);

        return serviceBroker
            .execute(
                CORE_API.DataTypeDecoratorStore.save,
                [vm.parentEntityRef, decoratorUpdateCommand]);
    };

    const doSavePropose = (command) => {
        const decoratorUpdateCommand = mkDataTypeUpdateCommand(
            vm.parentEntityRef,
            vm.checkedItemIds,
            vm.originalSelectedItemIds);
        command.dataTypeIds = vm.originalSelectedItemIds
        // Start with the original dataTypeIds from command
        let updatedDataTypeIds = new Set(command.dataTypeIds || []);

        // Add new IDs
        (decoratorUpdateCommand.addedDataTypeIds || []).forEach(id => updatedDataTypeIds.add(id));

        // Remove unwanted IDs
        (decoratorUpdateCommand.removedDataTypeIds || []).forEach(id => updatedDataTypeIds.delete(id));

        // Convert back to array and update the command
        command.dataTypeIds = Array.from(updatedDataTypeIds);

        return serviceBroker
            .execute(
                proposeDataFlowRemoteStore.proposeDataFlow(command)
                    .then(r => {
                        const response = r.data;
                        const commandLaunched = handleProposalValidation(response, false, null, false, goToWorkflow, PROPOSAL_TYPES.EDIT);
                    })
                    .catch(e => {
                        console.error("Error proposing data flow", e);
                    })
            )
    };

    const anySelected = () => {
        return notEmpty(vm.checkedItemIds);
    };

    const hasAnyChanges = () => {
        return !_.isEqual(vm.checkedItemIds.sort(), vm.originalSelectedItemIds.sort());
    };


    // -- INTERACT

    vm.typeUnchecked = (id, node) => {
        vm.checkedItemIds = _.without(vm.checkedItemIds, id);
        vm.onDirty(hasAnyChanges() && anySelected());
        node.usage = null;
    };

    vm.typeChecked = (id, node) => {
        // deselect any parents that are non-concrete
        let dt = vm.enrichedDataTypesById[id];
        while (dt) {
            const parent = vm.enrichedDataTypesById[dt.parentId];
            const parentIsAbstract = _.get(parent, ["dataType", "concrete"], true) === false;
            const parentIsRemovable = _.get(parent, ["usage", "isRemovable"], false) === true;
            if (parentIsAbstract && parentIsRemovable) {
                vm.typeUnchecked(parent.id, parent);
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

    const reload = (force = false) => {
        serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(result => {
                vm.rawDataTypes = result.data;
                vm.unknownDataType = _.find(vm.rawDataTypes, dt => dt.unknown);
            });

        const suggestedPromise = loadSuggestedDataTypes();
        const usagePromise = loadUsageData($q, serviceBroker, vm.parentEntityRef, force);

        return $q
            .all([usagePromise, suggestedPromise])
            .then(([usage, suggestions]) => {
                vm.used = usage;
                vm.suggestedDataTypes = suggestions;
                vm.onDirty(false);
                return postLoadActions(usage, suggestions);
            });
    };

    vm.save = () => {
        return doSave()
            .then(() => reload(true));
    };

    vm.savePropose = (command) => {
        return doSavePropose(command)
            .then(() => reload(true));
    }

    vm.disablePredicate = (node) => {
        const isAbstract = !node.dataType.concrete;
        const notUsed = node.usage === null;
        return isAbstract && notUsed;
    };

    vm.isReadonlyPredicate = (node) => {
        if (_.isNull(node.usage)) {
            return false;
        } else {
            return (vm.parentEntityRef.kind === "LOGICAL_DATA_FLOW")
                ? node.usage.readOnly || !node.usage.isRemovable
                : node.usage.readOnly;
        }
    };

    const determineMessage = () => {
        vm.showAllMessage = (vm.showAllDataTypes)
            ? "Show suggested data types"
            : "Show all data types";
    };

    vm.toggleShowAll = () => {
        vm.showAllDataTypes = !vm.showAllDataTypes;
        postLoadActions(vm.used, vm.suggestedDataTypes);
        determineMessage();
    };

    // -- LIFECYCLE

    vm.$onInit = () => {
        vm.onDirty(false);
        vm.onRegisterSave(vm.save);  // pass the save function out so it can be called (i.e. a save btn)
        vm.onRegisterSavePropose(vm.savePropose)
        determineMessage();

        reload(true);
    };

    vm.$onChanges = (c) => {
        if (c.parentEntityRef && vm.parentEntityRef) {
            reload(true);
        }
    };

    vm.nameProviderFn = d => d.dataType.name;

    vm.click = (key, item) => {
        vm.onSelect(item);
    };
}


controller.$inject = [
    "$q",
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

