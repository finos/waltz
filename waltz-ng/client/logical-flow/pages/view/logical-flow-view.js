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
import {dynamicSections} from "../../../dynamic-section/dynamic-section-definitions";
import {CORE_API} from "../../../common/services/core-api-utils";


const initialState = {
    logicalFlow: null,
    bookmarksSection: dynamicSections.bookmarksSection,
    changeLogSection: dynamicSections.changeLogSection,
    entityNamedNotesSection: dynamicSections.entityNamedNotesSection,
    entityDiagramsSection: dynamicSections.entityDiagramsSection
};



function mkHistoryObj(flow) {
    return {
        name: `${flow.source.name} to ${flow.target.name}`,
        kind: "LOGICAL_DATA_FLOW",
        state: "main.logical-flow.view",
        stateParams: { id: flow.id }
    };
}


function removeFromHistory(historyStore, flow) {
    if (! flow) { return; }

    const historyObj = mkHistoryObj(flow);

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


function controller($state,
                    $stateParams,
                    historyStore,
                    notification,
                    serviceBroker)
{
    const vm = initialiseData(this, initialState);

    const flowId = $stateParams.id;
    vm.entityReference = {
        id: flowId,
        kind: "LOGICAL_DATA_FLOW"
    };


    // -- LOAD ---

    serviceBroker
        .loadViewData(
            CORE_API.LogicalFlowStore.getById,
            [ flowId ])
        .then(r => vm.logicalFlow = r.data);


    const deleteLogicalFlow = () => {
        return serviceBroker
            .execute(CORE_API.LogicalFlowStore.removeFlow, [vm.logicalFlow.id])
            .then(r => {
                if (r.data > 0) {
                    notification.success(`Logical Flow between ${vm.logicalFlow.source.name} and ${vm.logicalFlow.target.name} deleted`);
                } else {
                    notification.error(r.message);
                }
                navigateToLastView($state, historyStore);
            });
    };

    const handleDeleteFlowResponse = (response) => {
        if (response > 0) {
            notification.success("Logical flow deleted");
            removeFromHistory(historyStore, vm.logicalFlow);
        } else {
            notification.error(response.message);
        }
    };

    vm.deleteFlow = () => {
        if (confirm("Are you sure you want to delete this flow ?")) {
            deleteLogicalFlow()
                .then(r => handleDeleteFlowResponse(r.data));
        }
    };
}


controller.$inject = [
    "$state",
    "$stateParams",
    "HistoryStore",
    "Notification",
    "ServiceBroker",
];


export default {
    template,
    controller,
    controllerAs: "ctrl"
};
