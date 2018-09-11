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

import {initialiseData} from "../common";
import {dynamicSections} from "../dynamic-section/dynamic-section-definitions";

import template from "./physical-specification-view.html";


const initialState = {
    visibility: {
        createReportOverlay: false,
        createReportButton: true,
        createReportBusy: false
    },
    createReportForm: {
        name: ""
    },
    logicalDataElements: [],
    selectedSpecDefinition: {},
    specDefinitions: [],
    specDefinitionCreate: {
        creating: false
    },
    bookmarksSection: dynamicSections.bookmarksSection,
    entityDiagramsSection: dynamicSections.entityDiagramsSection,
    changeLogSection: dynamicSections.changeLogSection
};


const addToHistory = (historyStore, spec) => {
    if (! spec) { return; }
    historyStore.put(
        spec.name,
        "PHYSICAL_SPECIFICATION",
        "main.physical-specification.view",
        { id: spec.id });
};



function loadFlowDiagrams(specId, $q, flowDiagramStore, flowDiagramEntityStore) {
    const ref = {
        id: specId,
        kind: "PHYSICAL_SPECIFICATION"
    };

    const selector = {
        entityReference: ref,
        scope: "EXACT"
    };

    const promises = [
        flowDiagramStore.findForSelector(selector),
        flowDiagramEntityStore.findForSelector(selector)
    ];
    return $q
        .all(promises)
        .then(([flowDiagrams, flowDiagramEntities]) => ({ flowDiagrams, flowDiagramEntities }));
}


function controller($q,
                    $stateParams,
                    applicationStore,
                    flowDiagramStore,
                    flowDiagramEntityStore,
                    historyStore,
                    logicalFlowStore,
                    orgUnitStore,
                    physicalSpecificationStore,
                    physicalFlowStore)
{
    const vm = initialiseData(this, initialState);

    const specId = $stateParams.id;
    const ref = {
        kind: "PHYSICAL_SPECIFICATION",
        id: specId
    };

    vm.entityReference = ref;

    // -- LOAD ---

    physicalSpecificationStore
        .getById(specId)
        .then(spec => vm.specification = spec)
        .then(spec => applicationStore.getById(spec.owningEntity.id))
        .then(app => vm.owningEntity = app)
        .then(app => orgUnitStore.getById(app.organisationalUnitId))
        .then(ou => vm.organisationalUnit = ou)
        .then(() => vm.entityReference = Object.assign({}, vm.entityReference, { name: vm.specification.name }))
        .then(() => addToHistory(historyStore, vm.specification));

    physicalFlowStore
        .findBySpecificationId(specId)
        .then(physicalFlows => vm.physicalFlows = physicalFlows);

    logicalFlowStore
        .findBySelector({ entityReference: ref, scope: "EXACT"})
        .then(logicalFlows => {
            vm.logicalFlows = logicalFlows;
            vm.logicalFlowsById = _.keyBy(logicalFlows, "id")
        });

    vm.loadFlowDiagrams = () => {
        loadFlowDiagrams(specId, $q, flowDiagramStore, flowDiagramEntityStore)
            .then(r => Object.assign(vm, r));
    };

    vm.loadFlowDiagrams();
}


controller.$inject = [
    "$q",
    "$stateParams",
    "ApplicationStore",
    "FlowDiagramStore",
    "FlowDiagramEntityStore",
    "HistoryStore",
    "LogicalFlowStore",
    "OrgUnitStore",
    "PhysicalSpecificationStore",
    "PhysicalFlowStore"
];


export default {
    template,
    controller,
    controllerAs: "ctrl"
};