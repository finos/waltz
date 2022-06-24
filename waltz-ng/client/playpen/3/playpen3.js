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
import template from "./playpen3.html";
import FlowVenn from "../../logical-flow/svelte/flow-venn/FlowVenn.svelte";
import OverlayDiagramBuilder from "./builder/OverlayDiagramBuilder.svelte";
import configA from "./builder/front-to-back-trade-data";
import configB from "./builder/front-to-back-operations-data";
import configC from "./builder/boap-ft";
import configD from "./builder/process-diag";
import DiagramBuilderControls from "./builder/DiagramBuilderControls.svelte"

const initialState = {
    b: {
        id: 2732,
        kind: "ORG_UNIT",
        name: "A Group"
    },
    a: {
        id: 2732,
        kind: "ORG_UNIT",
        name: "A Group"
    },
    FlowVenn,
    OverlayDiagramBuilder,
    configA,
    configB,
    configC,
    configD,
    DiagramBuilderControls
};

function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);
}

controller.$inject = [
    "ServiceBroker"
];


const view = {
    template,
    controller,
    controllerAs: "$ctrl",
    bindToController: true,
    scope: {}};


export default view;
