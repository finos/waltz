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
import {CORE_API} from "../../../common/services/core-api-utils";
import {initialiseData} from "../../../common";
import _ from "lodash";
import template from "./linked-diagrams-panel.html";
import {refToString, toEntityRef, toEntityRefWithKind} from "../../../common/entity-utils";
import {determineIfCreateAllowed} from "../../../flow-diagram/flow-diagram-utils";
import {displayError} from "../../../common/error-utils";
import {kindToViewState} from "../../../common/link-utils";
import toasts from "../../../svelte-stores/toast-store";
import {processDiagramStore} from "../../../svelte-stores/process-diagram-store";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {svelteCallToPromise} from "../../../common/promise-utils";

const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    selectedDiagram: null,
    visibility: {
        flowDiagramMode: null, // null | VIEW | EDIT
        makeNew: true
    },
};


function combineDiagrams(flowDiagrams = [],
                         svgDiagrams = [],
                         processDiagrams = [],
                         flowActions = []) {

    const flowDiagramUiState = kindToViewState("FLOW_DIAGRAM");
    const processDiagramUiState = kindToViewState("PROCESS_DIAGRAM");

    const convertFlowDiagramFn = d => {
        return {
            id: refToString(d),
            ref: toEntityRef(d),
            uiState: flowDiagramUiState,
            type: "Flow",
            name: d.name,
            icon: "random",
            description: d.description,
            actions: flowActions,
            lastUpdatedAt: d.lastUpdatedAt,
            lastUpdatedBy: d.lastUpdatedBy
        };
    };

    const convertProcessDiagramFn = d => {
        return {
            id: refToString(d),
            ref: toEntityRef(d),
            uiState: processDiagramUiState,
            type: "Process",
            name: d.name,
            icon: "cogs",
            description: d.description,
            lastUpdatedAt: d.lastUpdatedAt,
            lastUpdatedBy: d.lastUpdatedBy
        };
    };

    const convertSvgDiagramFn = d => {
        return {
            id: `ENTITY_SVG_DIAGRAM/${d.id}`,
            ref: toEntityRefWithKind(d, "ENTITY_SVG_DIAGRAM"),
            type: "Generic",
            name: d.name,
            icon: "picture-o",
            description: d.description,
            svg: d.svg
        }
    };


    const normalize = (normalizeFn, diagrams = []) => _.map(diagrams, normalizeFn);

    return _.chain([])
        .concat(normalize(convertFlowDiagramFn, flowDiagrams))
        .concat(normalize(convertSvgDiagramFn, svgDiagrams))
        .concat(normalize(convertProcessDiagramFn, processDiagrams))
        .orderBy(d => d.name.toLowerCase())
        .value();
}


function selectInitialDiagram(diagrams = [], selectedDiagram) {
    if (selectedDiagram) return selectedDiagram;
    return _.find(diagrams, d => d.type === "Generic");
}


function isAppAggregatingEntity(ref) {
    const aggregatingEntityKinds = [
        "APP_GROUP",
        "MEASURABLE",
        "ORG_UNIT",
        "PERSON"
    ];

    return _.includes(aggregatingEntityKinds, ref.kind);
}


function controller($q,
                    $state,
                    serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadProcessDiagrams = () => svelteCallToPromise(
        processDiagramStore
            .findBySelector(mkSelectionOptions(vm.parentEntityRef)));

    const loadFlowDiagrams = (force = true) => serviceBroker
        .loadViewData(
            CORE_API.FlowDiagramStore.findByEntityReference,
            [ vm.parentEntityRef ],
            { force })
        .then(r => r.data);

    const loadEntitySvgDiagrams = (force = false) => serviceBroker
        .loadViewData(
            CORE_API.EntitySvgDiagramStore.findByEntityReference,
            [ vm.parentEntityRef ],
            { force  })
        .then(r => r.data);

    const flowActions = [
        {
            name: "Clone",
            icon: "clone",
            execute: (diagram) => {
                const newName = prompt("What shall the cloned copy be called ?", `Copy of ${diagram.name}`);
                if (newName == null) {
                    toasts.warning("Clone cancelled");
                    return;
                }
                if (_.isEmpty(newName.trim())) {
                    toasts.warning("Clone cancelled, no name given");
                    return;
                }
                serviceBroker
                    .execute(CORE_API.FlowDiagramStore.clone, [diagram.ref.id, newName])
                    .then(() => {
                        toasts.success("Diagram cloned");
                        reload();
                    })
                    .catch(e => displayError("Failed to clone diagram", e));

            }}
    ];


    function reload() {
        const promises = [
            loadFlowDiagrams(true),
            loadEntitySvgDiagrams(false),
            loadProcessDiagrams()];
        return $q
            .all(promises)
            .then(([flowDiagrams = [], svgDiagrams = [], processDiagrams = []]) => {
                vm.diagrams = combineDiagrams(flowDiagrams, svgDiagrams, processDiagrams, flowActions);
                vm.selectedDiagram = selectInitialDiagram(vm.diagrams, vm.selectedDiagram);
                return vm.diagrams;
            });
    }

    vm.$onInit = () => {
        reload();
        vm.visibility.makeNew = determineIfCreateAllowed(vm.parentEntityRef.kind);
    };

    vm.onDiagramSelect = (diagram) => {
        vm.selectedDiagram = diagram;
        vm.visibility.flowDiagramMode = "VIEW";

        if(diagram.type === "Flow"){
            $state.go(kindToViewState("FLOW_DIAGRAM"), {id: diagram.ref.id});
        }
        if(diagram.type === "Process"){
            $state.go(kindToViewState("PROCESS_DIAGRAM"), {id: diagram.ref.id});
        }
    };

    vm.onDiagramDismiss = () => {
        vm.selectedDiagram = null;
    };

    vm.onEditorClose = () => {
        vm.selectedDiagram = null;
        reload();
    };

    vm.onDiagramEdit = () => {
        vm.visibility.flowDiagramMode = "EDIT";
    };

    vm.onMakeNewFlowDiagram = () => {
        const name = prompt("Please enter a name for the new diagram ?");
        if (name == null) {
            toasts.warning("Create cancelled");
            return;
        }
        if (_.isEmpty(name.trim())) {
            toasts.warning("Create cancelled, no name given");
            return;
        }

        serviceBroker
            .execute(CORE_API.FlowDiagramStore.makeNewForEntityReference, [vm.parentEntityRef, name])
            .then(r => {
                reload();
                return r.data;
            })
            .then(diagramId => {
                toasts.success("Diagram created, click edit if you wish to make changes");
                $state.go(kindToViewState("FLOW_DIAGRAM"), {id: diagramId});
            })
            .catch(e => displayError("Failed to create new diagram", e));
    };
}


controller.$inject = [
    "$q",
    "$state",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzLinkedDiagramsPanel"
};
