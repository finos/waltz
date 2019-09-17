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

import {initialiseData} from "../common";
import _ from "lodash";


import template from "./physical-flow-view.html";
import {dynamicSections} from "../dynamic-section/dynamic-section-definitions";
import {isRemoved, toEntityRef} from "../common/entity-utils";
import {CORE_API} from "../common/services/core-api-utils";
import {mkLinkGridCell} from "../common/grid-utils";


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
    mergeTarget: null
};



function mkHistoryObj(flow, spec) {
    return {
        name: spec.name,
        kind: "PHYSICAL_FLOW",
        state: "main.physical-flow.view",
        stateParams: { id: flow.id }
    };
}


function addToHistory(historyStore, flow, spec) {
    if (! flow || !spec) { return; }

    const historyObj = mkHistoryObj(flow, spec);

    historyStore.put(
        historyObj.name,
        historyObj.kind,
        historyObj.state,
        historyObj.stateParams);
}


function removeFromHistory(historyStore, flow, spec) {
    if (! flow || !spec) { return; }

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
                    $stateParams,
                    historyStore,
                    notification,
                    dynamicSectionManager,
                    physicalFlowStore,
                    physicalSpecificationStore,
                    serviceBroker)
{
    const vm = initialiseData(this, initialState);

    const entityReference = {
        id: $stateParams.id,
        kind: "PHYSICAL_FLOW"
    };


    vm.$onInit = () => {
        dynamicSectionManager.initialise("PHYSICAL_FLOW");
        vm.parentEntityRef = entityReference;

        const physicalFlowPromise = serviceBroker
            .loadViewData(
                CORE_API.PhysicalFlowStore.getById,
                [ vm.parentEntityRef.id ])
            .then(r => vm.physicalFlow = r.data);


        physicalFlowPromise
            .then(() => serviceBroker
                .loadViewData(
                    CORE_API.LogicalFlowStore.getById,
                    [vm.physicalFlow.logicalFlowId]))
            .then(r => vm.logicalFlow = r.data);


        physicalFlowPromise
            .then(physicalFlow => serviceBroker
                .loadViewData(
                    CORE_API.PhysicalSpecificationStore.getById,
                    [physicalFlow.specificationId]))
            .then(r => {
                vm.specification = r.data;
                vm.specificationReference = toEntityRef(r.data, "PHYSICAL_SPECIFICATION");
                addToHistory(historyStore, vm.physicalFlow, vm.specification);
            });
    };


    // -- INTERACT ---

    // -- INTERACT: delete
    const deleteSpecification = () => {
        physicalSpecificationStore.deleteById(vm.specification.id)
            .then(r => {
                if (r.outcome === "SUCCESS") {
                    notification.success(`Specification ${vm.specification.name} deleted`);
                } else {
                    notification.error(r.message);
                }
                navigateToLastView($state, historyStore);
            })
    };

    const deleteLogicalFlow = () => {
        serviceBroker
            .execute(CORE_API.LogicalFlowStore.removeFlow, [vm.physicalFlow.logicalFlowId])
            .then(r => {
                if (r.outcome === "SUCCESS") {
                    notification.success(`Logical Flow between ${vm.logicalFlow.source.name} and ${vm.logicalFlow.target.name} deleted`);
                } else {
                    notification.error(r.message);
                }
                navigateToLastView($state, historyStore);
            });
    };

    const handleDeleteFlowResponse = (response) => {
        if (response.outcome === "SUCCESS") {
            notification.success("Physical flow deleted");
            removeFromHistory(historyStore, vm.physicalFlow, vm.specification);

            if (response.isSpecificationUnused || response.isLastPhysicalFlow) {
                const deleteSpecText = `The specification ${vm.specification.name} is no longer referenced by any physical flow. Do you want to delete the specification?`;
                const deleteLogicalFlowText = `The logical flow described by this physical flow between ${vm.logicalFlow.source.name} and ${vm.logicalFlow.target.name} has no other physical flows. Do you want to delete the logical flow?`;

                if (response.isSpecificationUnused && confirm(deleteSpecText)) {
                    deleteSpecification();
                }

                if(response.isLastPhysicalFlow && confirm(deleteLogicalFlowText)) {
                    deleteLogicalFlow()
                }
            } else {
                navigateToLastView($state, historyStore);
            }
        } else {
            notification.error(response.message);
        }
    };

    vm.deleteFlow = () => {
        if (confirm("Are you sure you want to delete this flow ?")) {
            physicalFlowStore
                .deleteById(vm.parentEntityRef.id)
                .then(r => handleDeleteFlowResponse(r));
        }
    };


    // -- INTERACT: de-dupe
    const loadPotentialMergeTargets = () => {
        const potentialFlowsPromise = serviceBroker
            .loadViewData(
                CORE_API.PhysicalFlowStore.findByLogicalFlowId,
                [ vm.logicalFlow.id ],
                { force: true })
            .then(r => r.data);

        const potentialSpecsPromise = serviceBroker
            .loadViewData(
                CORE_API.PhysicalSpecificationStore.findByLogicalFlow,
                [ vm.logicalFlow.id ])
            .then(r => r.data);

        $q.all([potentialFlowsPromise, potentialSpecsPromise])
            .then(([flows, specs]) => {
                const specsById = _.keyBy(specs, s => s.id);
                vm.potentialMergeTargets = _
                    .chain(flows)
                    .reject(f => f.id === vm.parentEntityRef.id)
                    .reject(isRemoved)
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
        vm.mode = vm.mode === modes.DUPLICATE
            ? vm.onShowOverview()
            : modes.DUPLICATE;

        loadPotentialMergeTargets();
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
                    [ fromId , toId ])
                .then(notification.warning("Flow has been marked as duplicate"))
                .then(() => $state.reload())
        } else {
            notification.info("De-duplication cancelled");
        }
    };



    // -- INTERACT: other
    vm.onShowOverview = () => {
        vm.mergeTarget = null;
        vm.mode = modes.OVERVIEW;
    };

}


controller.$inject = [
    "$q",
    "$state",
    "$stateParams",
    "HistoryStore",
    "Notification",
    "DynamicSectionManager",
    "PhysicalFlowStore",
    "PhysicalSpecificationStore",
    "ServiceBroker"
];


export default {
    template,
    controller,
    controllerAs: "ctrl"
};
