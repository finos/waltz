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