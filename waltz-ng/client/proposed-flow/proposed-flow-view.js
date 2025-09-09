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
import template from "./proposed-flow-view.html";
import FlowWizard from "./components/FlowWizard.svelte";
import ProposedFlowDetails from "./components/ProposedFlowDetails.svelte";
import ProposedFlowActions from "./components/ProposedFlowActions.svelte";
import FlowTransitionDetails from "./components/FlowTransitionDetails.svelte";
import { CORE_API } from "../common/services/core-api-utils";


const initialState = {
    proposedFlow: null,
    FlowWizard,
    ProposedFlowDetails,
    ProposedFlowActions,
    FlowTransitionDetails
};

function controller($q,
                    $state,
                    $stateParams,
                    $window,
                    proposedFlowStore,
                    serviceBroker)
{
    const vm = initialiseData(this, initialState);

    const entityReference = {
        id: $stateParams.id,
        kind: "PROPOSED_FLOW"
    };

     vm.$onInit = () => {
            vm.parentEntityRef = entityReference;
            
            serviceBroker
                .loadViewData(
                    CORE_API.ProposedFlowStore.getById,
                    [vm.parentEntityRef.id])
                .then(r => {
                    vm.proposedFlow = r.data;
                    vm.usedDataTypes = (vm.proposedFlow?.flowDef?.dataTypeIds || []).map(id => ({ dataTypeId: id }));
                    vm.dataTypeHelpText = "Data types used by this flow.";
                });
    };

    vm.refreshState = () => {
        $state.reload();
    }
    
}


controller.$inject = [
    "$q",
    "$state",
    "$stateParams",
    "$window",
    "ProposedFlowStore",
    "ServiceBroker"
];


export default {
    template,
    controller,
    controllerAs: "$ctrl"
};
