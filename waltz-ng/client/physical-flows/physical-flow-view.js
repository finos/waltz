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

import {initialiseData} from "../common";
import _ from "lodash";

import template from "./physical-flow-view.html";
import {CORE_API} from "../common/services/core-api-utils";
import {toEntityRefWithKind} from "../common/entity-utils";
import toasts from "../svelte-stores/toast-store";
import {displayError} from "../common/error-utils";
import {copyTextToClipboard} from "../common/browser-utils";
import {getDataFlowProposalsRatingScheme, isDataFlowProposalsEnabled} from "../common/utils/settings-util";
import {proposeDataFlowRemoteStore} from "../svelte-stores/propose-data-flow-remote-store";
import ReasonSelection from "../data-types/components/usage-panel/ReasonSelection.svelte";
import {
    deleteFlowReason,
    duplicateProposeFlowMessage,
    existingProposeFlowId
} from "../data-flow/components/svelte/propose-data-flow/propose-data-flow-store";
import pageInfo from "../svelte-stores/page-navigation-store";
import {PROPOSAL_TYPES} from "../common/constants";
import {handleProposalValidation} from "../common/utils/proposalValidation";
import {buildProposalFlowCommand} from "../common/utils/propose-flow-command-util";


const modes = {
    OVERVIEW: "OVERVIEW",
    DUPLICATE: "DUPLICATE"
};


const initialState = {
    mode: modes.OVERVIEW,
    physicalFlow: null,
    selected: {
        entity: null,
        incoming: [],
        outgoing: []
    },
    specification: null,
    visibility: {
        diagramEditor: false,
        overviewEditor: false
    },
    potentialMergeTargets: [],
    mergeTarget: null,
    settings: null,
    dataFlowProposalsEnabled: null,
    dataFlowProposalsRatingSchemeSetting: null,
    isReasonSelectionOpen: false,
    ratingSchemeExtId: null,
    selectedReason: null,
    dataType: [],
    type: PROPOSAL_TYPES.DELETE,
    ReasonSelection
};


function mkHistoryObj(flow, spec) {
    return {
        name: spec.name,
        kind: "PHYSICAL_FLOW",
        state: "main.physical-flow.view",
        stateParams: {id: flow.id}
    };
}

function goToWorkflow(proposedFlowId) {
    pageInfo.set({
        state: "main.proposed-flow.view", params: {
            id: proposedFlowId
        }
    })
    existingProposeFlowId.set(null)
    duplicateProposeFlowMessage.set(null)
}

function addToHistory(historyStore, flow, spec) {
    if (!flow || !spec) {
        return;
    }

    const historyObj = mkHistoryObj(flow, spec);

    historyStore.put(
        historyObj.name,
        historyObj.kind,
        historyObj.state,
        historyObj.stateParams);
}


function removeFromHistory(historyStore, flow, spec) {
    if (!flow || !spec) {
        return;
    }

    const historyObj = mkHistoryObj(flow, spec);

    historyStore.remove(
        historyObj.name,
        historyObj.kind,
        historyObj.state,
        historyObj.stateParams);
}


function navigateToLastView($state, historyStore) {
    const lastHistoryItem = historyStore.getAll()[0];
    if (lastHistoryItem) {
        $state.go(lastHistoryItem.state, lastHistoryItem.stateParams);
    } else {
        $state.go("main.home");
    }
}


function controller($q,
                    $state,
                    $scope,
                    $stateParams,
                    $window,
                    historyStore,
                    physicalFlowStore,
                    physicalSpecificationStore,
                    serviceBroker) {
    const vm = initialiseData(this, initialState);

    const entityReference = {
        id: $stateParams.id,
        kind: "PHYSICAL_FLOW"
    };


    vm.$onInit = () => {
        vm.parentEntityRef = entityReference;


        const settingsPromise = serviceBroker
            .loadViewData(CORE_API.SettingsStore.findAll, [])
            .then(r => {
                vm.settings = r.data;
                vm.dataFlowProposalsEnabled = isDataFlowProposalsEnabled(vm.settings)
                vm.ratingSchemeExtId = getDataFlowProposalsRatingScheme(vm.settings);
            });


        const physicalFlowPromise = serviceBroker
            .loadViewData(
                CORE_API.PhysicalFlowStore.getById,
                [vm.parentEntityRef.id])
            .then(r => vm.physicalFlow = r.data);

        physicalFlowPromise
            .then(() => serviceBroker
                .loadViewData(
                    CORE_API.LogicalFlowStore.getById,
                    [vm.physicalFlow.logicalFlowId]))
            .then(r => vm.logicalFlow = r.data);

        physicalFlowPromise
            .then(() => serviceBroker
                .loadViewData(
                    CORE_API.LogicalFlowStore.findPermissionsForFlow,
                    [vm.physicalFlow.logicalFlowId]))
            .then(r => vm.canEdit = _.some(
                r.data,
                d => _.includes(["ADD", "UPDATE", "REMOVE"], d)));

        physicalFlowPromise
            .then(physicalFlow => serviceBroker
                .loadViewData(
                    CORE_API.PhysicalSpecificationStore.getById,
                    [physicalFlow.specificationId]))
            .then(r => {
                vm.specification = r.data;
                vm.specificationReference = toEntityRefWithKind(r.data, "PHYSICAL_SPECIFICATION");
                addToHistory(historyStore, vm.physicalFlow, vm.specification);
            });
    };

    const launchCommand = () => {

        return serviceBroker
            .loadViewData(CORE_API.DataTypeDecoratorStore.findDatatypeUsageCharacteristics, [vm.specificationReference])
            .then(dataTypeResponse => {
                const ids = dataTypeResponse.data.map(item => item.dataTypeId);
                vm.dataType = ids;

                if (vm.physicalFlow && vm.specification && vm.logicalFlow) {
                    return buildProposalFlowCommand({
                        physicalFlow: vm.physicalFlow,
                        specification: vm.specification,
                        logicalFlow: vm.logicalFlow,
                        dataType: vm.dataType,
                        selectedReason: vm.selectedReason,
                        proposalType: PROPOSAL_TYPES.DELETE
                    });
                }
            })
            .catch(error => {
                console.error("Error in launchCommand:", error);
            });
    };

    // -- INTERACT ---

    // -- INTERACT: delete
    const deleteSpecification = () => {
        serviceBroker
            .execute(CORE_API.PhysicalSpecificationStore.deleteById, [vm.specification.id])
            .then(r => toasts.success(`Specification ${vm.specification.name} deleted`))
            .catch(e => displayError("Could not delete specification", e))
            .finally(() => navigateToLastView($state, historyStore));
    };

    const deleteLogicalFlow = () => {
        serviceBroker
            .execute(CORE_API.LogicalFlowStore.removeFlow, [vm.physicalFlow.logicalFlowId])
            .then(r => toasts.success(`Logical Flow between ${vm.logicalFlow.source.name} and ${vm.logicalFlow.target.name} deleted`))
            .catch(e => displayError("Could not delete logical flow", e))
            .finally(() => navigateToLastView($state, historyStore));
    };

    const handleDeleteFlowResponse = (response) => {
        if (response.outcome === "SUCCESS") {
            toasts.success("Physical flow deleted");
            removeFromHistory(historyStore, vm.physicalFlow, vm.specification);

            if (response.isSpecificationUnused || response.isLastPhysicalFlow) {
                const deleteSpecText = `The specification ${vm.specification.name} is no longer referenced by any physical flow. Do you want to delete the specification?`;
                const deleteLogicalFlowText = `The logical flow described by this physical flow between ${vm.logicalFlow.source.name} and ${vm.logicalFlow.target.name} has no other physical flows. Do you want to delete the logical flow?`;

                if (response.isSpecificationUnused && confirm(deleteSpecText)) {
                    deleteSpecification();
                }

                if (response.isLastPhysicalFlow && confirm(deleteLogicalFlowText)) {
                    deleteLogicalFlow()
                }
            } else {
                navigateToLastView($state, historyStore);
            }
        } else {
            toasts.error(response.message);
        }
    };

    vm.deleteFlow = () => {
        if (confirm("Are you sure you want to delete this flow ?")) {
            physicalFlowStore
                .deleteById(vm.parentEntityRef.id)
                .then(r => handleDeleteFlowResponse(r));
        }
    };

    vm.showReason = (value) => {
        $scope.$applyAsync(() => {
            vm.isReasonSelectionOpen = value;
        });
    };

    vm.proposeDeleteFlow = () => {
        launchCommand().then(command => {
            if (command) {
                proposeDataFlowRemoteStore.proposeDataFlow(command)
                    .then(r => {
                        const response = r.data;
                        const commandLaunched = handleProposalValidation(response, false, null, false, goToWorkflow, PROPOSAL_TYPES.DELETE);
                    })
                    .catch(e => console.error("Error proposing data flow", e));
            }
        });
    };

    deleteFlowReason.subscribe(value => {
        vm.selectedReason = value;
    });

    // -- INTERACT: de-dupe
    const loadPotentialMergeTargets = () => {
        const selector = {
            entityReference: {id: vm.logicalFlow.id, kind: "LOGICAL_DATA_FLOW"},
            scope: "EXACT"
        };

        const potentialFlowsPromise = serviceBroker
            .loadViewData(
                CORE_API.PhysicalFlowStore.findBySelector,
                [selector],
                {force: true})
            .then(r => r.data);

        const potentialSpecsPromise = serviceBroker
            .loadViewData(
                CORE_API.PhysicalSpecificationStore.findBySelector,
                [selector])
            .then(r => r.data);

        $q.all([potentialFlowsPromise, potentialSpecsPromise])
            .then(([flows, specs]) => {
                const specsById = _.keyBy(specs, s => s.id);
                vm.potentialMergeTargets = _
                    .chain(flows)
                    .reject(f => f.id === vm.parentEntityRef.id)
                    .map(f => ({
                        physicalFlow: f,
                        physicalSpec: specsById[f.specificationId]
                    }))
                    .reject(d => d.physicalSpec === null)
                    .orderBy(d => d.physicalSpec.name)
                    .value();
            });
    };

    vm.onShowMarkAsDuplicate = () => {
        if (vm.mode !== modes.DUPLICATE) {
            loadPotentialMergeTargets();
            vm.mode = modes.DUPLICATE;
        } else {
            vm.onShowOverview();
        }
    };

    vm.onSelectMergeTarget = (t) => {
        vm.mergeTarget = t;
    };

    vm.onClearMergeTarget = () => {
        vm.mergeTarget = null;
    };

    vm.onMergePhysicalFlow = (fromId, toId) => {
        if (confirm("Are you sure you want to de-duplicate these flows ?")) {
            serviceBroker
                .loadViewData(
                    CORE_API.PhysicalFlowStore.merge,
                    [fromId, toId])
                .then(toasts.warning("Flow has been marked as duplicate"))
                .then(() => $state.reload())
        } else {
            toasts.info("De-duplication cancelled");
        }
    };


    // -- INTERACT: other
    vm.onShowOverview = () => {
        vm.mergeTarget = null;
        vm.mode = modes.OVERVIEW;
    };


    vm.sharePageLink = () => {
        const viewUrl = $state.href("main.physical-flow.external-id", {externalId: vm.physicalFlow.externalId});
        copyTextToClipboard(`${$window.location.origin}${viewUrl}`)
            .then(() => toasts.success("Copied link to clipboard"))
            .catch(e => displayError("Could not copy link to clipboard", e));
    }
}


controller.$inject = [
    "$q",
    "$state",
    "$scope",
    "$stateParams",
    "$window",
    "HistoryStore",
    "PhysicalFlowStore",
    "PhysicalSpecificationStore",
    "ServiceBroker"
];


export default {
    template,
    controller,
    controllerAs: "ctrl"
};
