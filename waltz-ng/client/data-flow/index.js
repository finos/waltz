
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

import angular from "angular";

import {registerComponents} from "../common/module-utils";
import ApplicationFlowSummaryGraph from "./components/application-flow-summary-graph/application-flow-summary-graph";
import ApplicationFlowSummaryPane from "./components/application-flow-summary-pane/application-flow-summary-pane";
import DataFlowSection from "./components/data-flow-section/data-flow-section";
import LogicalFlowViewGrid from "./components/logical-flow-view-grid/logical-flow-view-grid";


function setup() {
    const module = angular.module("waltz.data-flow", []);

    registerComponents(
        module,
        [
            ApplicationFlowSummaryGraph,
            ApplicationFlowSummaryPane,
            DataFlowSection,
            LogicalFlowViewGrid]);

    return module.name;
}


export default setup;
