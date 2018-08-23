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
import {toEntityRef} from "../common/entity-utils";
import {CORE_API} from "../common/services/core-api-utils";


const initialState = {
    logicalDataElements: [],
    physicalFlow: null,
    selected: {
        entity: null,
        incoming: [],
        outgoing: []
    },
    specification: null,
    selectedSpecDefinition: {},
    selectableSpecDefinitions: [],
    tour: [],
    visibility: {
        diagramEditor: false,
        overviewEditor: false
    },
    bookmarksSection: dynamicSections.bookmarksSection,
    changeLogSection: dynamicSections.changeLogSection,
    entityNamedNotesSection: dynamicSections.entityNamedNotesSection,
    entityDiagramsSection: dynamicSections.entityDiagramsSection
};



function mkHistoryObj(flow, spec) {
    return {
        name: spec.name,
        kind: 'PHYSICAL_FLOW',
        state: 'main.physical-flow.view',
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
        $state.go('main.home');
    }
}


function getSelectedSpecDefinition(specDefinitions = [], selectedSpecDefId = null) {
    if (selectedSpecDefId) {
        const defsById = _.keyBy(specDefinitions, 'id');
        return defsById[selectedSpecDefId];
    } else {
        // find the active definition
        return _.find(specDefinitions, d => d.status === 'ACTIVE');
    }
}


function getSelectableSpecDefinitions(specDefinitions = [], selectedSpecDef) {
    if (selectedSpecDef) {
        return _.filter(specDefinitions, sd => sd.id !== selectedSpecDef.id);
    }

    return specDefinitions;
}


function controller($q,
                    $state,
                    $stateParams,
                    historyStore,
                    notification,
                    physicalFlowStore,
                    physicalSpecDefinitionStore,
                    physicalSpecDefinitionFieldStore,
                    physicalSpecDefinitionSampleFileStore,
                    physicalSpecificationStore,
                    serviceBroker,
                    tourService)
{
    const vm = initialiseData(this, initialState);

    const flowId = $stateParams.id;
    vm.entityReference = {
        id: flowId,
        kind: 'PHYSICAL_FLOW'
    };


    // -- LOAD ---

    const physicalFlowPromise = serviceBroker
        .loadViewData(
            CORE_API.PhysicalFlowStore.getById,
            [ flowId ])
        .then(r => vm.physicalFlow = r.data);

    physicalFlowPromise
        .then(physicalFlow => serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.getById,
                [vm.physicalFlow.logicalFlowId]))
        .then(r => vm.logicalFlow = r.data);

    const specPromise = physicalFlowPromise
        .then(physicalFlow => serviceBroker
            .loadViewData(
                CORE_API.PhysicalSpecificationStore.getById,
                [physicalFlow.specificationId]))
        .then(r => {
            vm.specification = r.data;
            vm.specificationReference = toEntityRef(r.data, 'PHYSICAL_SPECIFICATION');
        });

    // spec definitions
    const loadSpecDefinitions = (force = false) => physicalSpecDefinitionStore
        .findForSpecificationId(vm.physicalFlow.specificationId)
        .then(specDefs => {
            vm.selectedSpecDefinition.def = getSelectedSpecDefinition(specDefs, vm.physicalFlow.specificationDefinitionId);
            vm.selectableSpecDefinitions = getSelectableSpecDefinitions(specDefs, vm.selectedSpecDefinition.def);
        })
        .then(() => {
            if (vm.selectedSpecDefinition.def) {
                const specDefFieldPromise = physicalSpecDefinitionFieldStore
                    .findForSpecDefinitionId(vm.selectedSpecDefinition.def.id);

                const specDefSampleFilePromise = physicalSpecDefinitionSampleFileStore
                    .findForSpecDefinitionId(vm.selectedSpecDefinition.def.id);

                const selectionOptions = {
                    scope: 'EXACT',
                    entityReference: { kind: 'PHYSICAL_SPECIFICATION', id:vm.selectedSpecDefinition.def.specificationId }
                };

                const logicalElementsPromise = serviceBroker
                    .loadViewData(CORE_API.LogicalDataElementStore.findBySelector, [ selectionOptions ], { force })
                    .then(r => r.data);

                $q.all([specDefFieldPromise, specDefSampleFilePromise, logicalElementsPromise])
                    .then(([fields, file, logicalElements]) => {
                        vm.selectedSpecDefinition.fields = fields;
                        vm.selectedSpecDefinition.sampleFile = file;
                        vm.logicalDataElements = logicalElements;
                    });
            }
        });

    specPromise
        .then(() => loadSpecDefinitions());

    // tour
    specPromise
        .then(() => tourService.initialiseForKey('main.physical-flow.view', true))
        .then(tour => vm.tour = tour)
        .then(() => addToHistory(historyStore, vm.physicalFlow, vm.specification));


    const deleteSpecification = () => {
        physicalSpecificationStore.deleteById(vm.specification.id)
            .then(r => {
                if (r.outcome === 'SUCCESS') {
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
                if (r.outcome === 'SUCCESS') {
                    notification.success(`Logical Flow between ${vm.logicalFlow.source.name} and ${vm.logicalFlow.target.name} deleted`);
                } else {
                    notification.error(r.message);
                }
                navigateToLastView($state, historyStore);
            });
    };

    const handleDeleteFlowResponse = (response) => {
        if (response.outcome === 'SUCCESS') {
            notification.success('Physical flow deleted');
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
        if (confirm('Are you sure you want to delete this flow ?')) {
            physicalFlowStore
                .deleteById(flowId)
                .then(r => handleDeleteFlowResponse(r));
        }
    };

    vm.updateSpecDefinitionId = (newSpecDef) => {
        if (confirm('Are you sure you want to change the specification definition version used by this flow ?')) {
            physicalFlowStore
                .updateSpecDefinitionId(flowId, {
                    newSpecDefinitionId: newSpecDef.id
                })
                .then(r => {
                    vm.physicalFlow.specificationDefinitionId = newSpecDef.id;
                    loadSpecDefinitions();
                    notification.success('Specification definition version updated successfully');
                });
        }
    };

    vm.updateFieldDescription = (fieldId, change) => {
        const cmd = { newDescription: change.newVal };
        serviceBroker
            .execute(CORE_API.PhysicalSpecDefinitionFieldStore.updateDescription, [fieldId, cmd])
            .then(result => {
                if (result) {
                    notification.success(`Updated description for field`);
                    loadSpecDefinitions(true);
                } else {
                    notification.error(`Could not update field description`);
                }
            });
    };

    vm.updateLogicalDataElement = (fieldId, change) => {
        const cmd = { newLogicalDataElement: change.newVal };
        serviceBroker
            .execute(CORE_API.PhysicalSpecDefinitionFieldStore.updateLogicalElement, [fieldId, cmd])
            .then(result => {
                if (result) {
                    notification.success(`Updated logical data element for field`);
                    loadSpecDefinitions(true);
                } else {
                    notification.error(`Could not update logical data element`);
                }
            });
    };

}


controller.$inject = [
    '$q',
    '$state',
    '$stateParams',
    'HistoryStore',
    'Notification',
    'PhysicalFlowStore',
    'PhysicalSpecDefinitionStore',
    'PhysicalSpecDefinitionFieldStore',
    'PhysicalSpecDefinitionSampleFileStore',
    'PhysicalSpecificationStore',
    'ServiceBroker',
    'TourService'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};
