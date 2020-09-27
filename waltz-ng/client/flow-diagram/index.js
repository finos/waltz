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


import FlowDiagramStore from "./services/flow-diagram-store";
import FlowDiagramAnnotationStore from "./services/flow-diagram-annotation-store";
import FlowDiagramEntityStore from "./services/flow-diagram-entity-store";

import FlowDiagramStateService from "./services/flow-diagram-state-service";

import flowDiagram from "./components/diagram/flow-diagram";
import flowDiagramLogicalFlowPopup from "./components/editor/flow-diagram-logical-flow-popup";
import flowDiagramNodePopup from "./components/editor/flow-diagram-node-popup";
import flowDiagramPhysicalFlowPopup from "./components/editor/flow-diagram-physical-flow-popup";
import flowDiagramAnnotationPopup from "./components/editor/flow-diagram-annotation-popup";
import flowDiagramEditor from "./components/editor/flow-diagram-editor";
import FlowDiagramsPanelView from "./components/diagrams-panel/view/flow-diagrams-panel-view";
import FlowDiagramMeasurableAssociations from "./components/measurable-associations/flow-diagram-measurable-associations";
import FlowDiagramChangeInitiativeAssociations from "./components/change-initiative-associations/flow-diagram-change-initiative-associations";
import FlowDiagramInfoPopup from "./components/editor/flow-diagram-info-popup";

import {registerComponents, registerServices, registerStores} from "../common/module-utils";

import routes from "./routes";


export default () => {

    const module = angular.module("waltz.flow-diagram", []);

    module
        .component("waltzFlowDiagram", flowDiagram)
        .component("waltzFlowDiagramLogicalFlowPopup", flowDiagramLogicalFlowPopup)
        .component("waltzFlowDiagramNodePopup", flowDiagramNodePopup)
        .component("waltzFlowDiagramPhysicalFlowPopup", flowDiagramPhysicalFlowPopup)
        .component("waltzFlowDiagramAnnotationPopup", flowDiagramAnnotationPopup)
        .component("waltzFlowDiagramEditor", flowDiagramEditor)
    ;

    module
        .config(routes)
    ;

    registerServices(module, [
        FlowDiagramStateService
    ]);

    registerComponents(module, [
        FlowDiagramMeasurableAssociations,
        FlowDiagramChangeInitiativeAssociations,
        FlowDiagramsPanelView,
        FlowDiagramInfoPopup
    ]);

    registerStores(module, [
        FlowDiagramStore,
        FlowDiagramAnnotationStore,
        FlowDiagramEntityStore
    ]);

    return module.name;
};
