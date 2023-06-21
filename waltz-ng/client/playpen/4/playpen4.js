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
import {initialiseData} from "../../common/index";
import template from "./playpen4.html";
import {gridService} from "../../report-grid/components/svelte/report-grid-service";
import {mkSelectionOptions} from "../../common/selector-utils";


const initialState = {
    parentEntityRef: {kind: "ORG_UNIT", id: 10524}, //10524
};


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.selectionOptions = mkSelectionOptions(vm.parentEntityRef);

    gridService.selectGrid(3, vm.selectionOptions)
        .then((d) => console.log("loaded grid", d));

    gridService.gridDefinition.subscribe(d => console.log({gridDef: d}))
    gridService.gridInstance.subscribe(d => console.log({gridInstance: d}))
    gridService.gridMembers.subscribe(d => console.log({gridMembers: d}))
}


controller.$inject = [
    "$q",
    "ServiceBroker"
];


const view = {
    template,
    controller,
    controllerAs: "$ctrl",
    bindToController: true,
    scope: {}
};


export default view;
