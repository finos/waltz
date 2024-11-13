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

import {initialiseData} from "../../../common/index";


import template from "./logical-flow-view.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import toasts from "../../../svelte-stores/toast-store";
import _ from "lodash";
import {displayError} from "../../../common/error-utils";
import AlignedDataTypesList from "../../components/aligned-data-types-list/AlignedDataTypesList.svelte";
import {copyTextToClipboard} from "../../../common/browser-utils";


const initialState = {
    logicalFlow: null,
    isDraft: false,
    isRemoved: false,
    canEdit: false,
    canRestore: false,
    canRemove: false,
    updateCommand: {
        readOnly: false,
    },
    AlignedDataTypesList
};

function controller($q,
                    $state,
                    $stateParams,
                    $window,
                    serviceBroker,
                    historyStore)
{
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const flowId = $stateParams.id;
        vm.entityReference = {
            id: flowId,
            kind: "LOGICAL_DATA_FLOW"
        };

        // -- LOAD ---

        const flowPromise = serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.getById,
                [ flowId ])
            .then(r => vm.logicalFlow = r.data)


        flowPromise
            .then(r => {
                historyStore.put(
                    `Logical Flow: ${vm.logicalFlow.source.name} to ${vm.logicalFlow.target.name}`,
                    "LOGICAL_DATA_FLOW",
                    "main.logical-flow.view",
                    { id: flowId });
            });

        const permissionPromise = serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.findPermissionsForFlow,
                [ flowId ])
            .then(r => {
                vm.canEdit = _.some(r.data, d => _.includes(["ADD", "UPDATE", "REMOVE"], d));
            });

        $q.all([flowPromise, permissionPromise]).then(() => {
            vm.isDraft = vm.logicalFlow.entityLifecycleStatus === "PENDING";
            vm.isRemoved = vm.logicalFlow.entityLifecycleStatus === "REMOVED" || vm.logicalFlow.isRemoved;
            vm.isReadOnly = vm.logicalFlow.isReadOnly;
            vm.canRemove = vm.canEdit && !vm.isRemoved;
            vm.canRestore = vm.canEdit && vm.isRemoved;
        });

    };

    const onToggleReadOnly = () => {
        const changedField = !vm.logicalFlow.isReadOnly;
        vm.updateCommand.readOnly = changedField;
        return serviceBroker
            .execute(CORE_API.LogicalFlowStore.updateReadOnly, [vm.updateCommand, vm.logicalFlow.id])
            .then(() => {
                toasts.success("Successfully made the flow " + (changedField ? `read only` : `editable`) + '.');
                setTimeout(() => {
                    $window.location.reload();
                }, 600);
            })
            .catch(e => {
                toasts.error(e.data.message);
            });
    }

    const removeLogicalFlow = () => {
        return serviceBroker
            .execute(CORE_API.LogicalFlowStore.removeFlow, [vm.logicalFlow.id])
            .then(r => {
                if (r.data > 0) {
                    toasts.success(`Logical Flow between ${vm.logicalFlow.source.name} and ${vm.logicalFlow.target.name} removed`);
                } else {
                    toasts.error(r.message);
                }
                $window.location.reload();
            })
            .catch(e => displayError("Unable to remove flow", e));
    };

    const restoreLogicalFlow = () => {
        return serviceBroker
            .execute(CORE_API.LogicalFlowStore.restoreFlow, [vm.logicalFlow.id])
            .then(r => {
                if (r.data > 0) {
                    toasts.success(`Logical Flow between ${vm.logicalFlow.source.name} and ${vm.logicalFlow.target.name} has been restored`);
                } else {
                    toasts.error(r.message);
                }
                $window.location.reload();
            })
            .catch(e => displayError("Unable to restore flow", e));
    };

    const handleRemoveFlowResponse = (response) => {
        if (response > 0) {
            toasts.success("Logical flow removed");
        } else {
            toasts.error(response.message);
        }
    };

    vm.removeFlow = () => {
        if (confirm("Are you sure you want to remove this flow ?")) {
            removeLogicalFlow()
                .then(r => handleRemoveFlowResponse(r.data));
        }
    };

    vm.onToggleReadOnly = () => {
        onToggleReadOnly();
    }

    vm.restoreFlow = () => {
        if (confirm("Are you sure you want to restore this flow ?")) {
            console.log("restoring", vm.logicalFlow);
            restoreLogicalFlow();
        }
    };

    vm.sharePageLink = () => {
        const viewUrl = $state.href("main.logical-flow.external-id", { externalId: vm.logicalFlow.externalId });
        copyTextToClipboard(`${$window.location.origin}${viewUrl}`)
            .then(() => toasts.success("Copied link to clipboard"))
            .catch(e => displayError("Could not copy link to clipboard", e));
    }
}


controller.$inject = [
    "$q",
    "$state",
    "$stateParams",
    "$window",
    "ServiceBroker",
    "HistoryStore"
];


export default {
    template,
    controller,
    controllerAs: "ctrl"
};
