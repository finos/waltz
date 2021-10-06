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
import ProcessDiagram, {diagram} from "./components/process-diagram/ProcessDiagram.svelte";
import template from "./process-diagram-view.html";
import {svelteCallToPromise} from "../common/promise-utils";
import {processDiagramStore} from "../svelte-stores/process-diagram-store";
import {initData} from "./components/process-diagram/diagram-store";
import {processDiagramEntityStore} from "../svelte-stores/process-diagram-entity-store";

const initialState = {
    visibility: {},
    ProcessDiagram
};


const addToHistory = (historyStore, diagram) => {
    if (! diagram) { return; }
    historyStore.put(
        diagram.name,
        "PROCESS_DIAGRAM",
        "main.process-diagram.view",
        { id: diagram.id });
};



function controller($q, $stateParams, historyStore)
{
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const id = $stateParams.id;
        vm.parentEntityRef = { id, kind: "PROCESS_DIAGRAM" };

        const diagrmaPromise = svelteCallToPromise(processDiagramStore.getById(id));
        const alignmentPromise = svelteCallToPromise(processDiagramEntityStore.findApplicationAlignmentsByDiagramId(id));


        $q.all([diagrmaPromise, alignmentPromise])
            .then(([d, alignments]) => {
                vm.diagram = d.diagram;
                vm.entities = d.entities;
                addToHistory(historyStore, d.diagram);
                console.log({vm})
                const layout = JSON.parse(vm.diagram.layoutData);
                if (layout) {
                    initData(diagram, layout, alignments);
                }
            });

    }
}

controller.$inject = [
    "$q",
    "$stateParams",
    "HistoryStore",
];

const view = {
    template,
    controller,
    controllerAs: "$ctrl"
};

export default view;