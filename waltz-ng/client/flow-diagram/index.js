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
import FlowDiagramsPanelBrowse from "./components/diagrams-panel/browse/flow-diagrams-panel-browse";
import FlowDiagramsPanelView from "./components/diagrams-panel/view/flow-diagrams-panel-view";
import FlowDiagramMeasurableAssociations from "./components/measurable-associations/flow-diagram-measurable-associations";
import FlowDiagramChangeInitiativeAssociations from "./components/change-initiative-associations/flow-diagram-change-initiative-associations";
import FlowDiagramInfoPopup from "./components/editor/flow-diagram-info-popup";

import {registerComponents, registerServices, registerStores} from "../common/module-utils";

import routes from "./routes";


export default () => {

    const module = angular.module('waltz.flow-diagram', []);

    module
        .component('waltzFlowDiagram', flowDiagram)
        .component('waltzFlowDiagramLogicalFlowPopup', flowDiagramLogicalFlowPopup)
        .component('waltzFlowDiagramNodePopup', flowDiagramNodePopup)
        .component('waltzFlowDiagramPhysicalFlowPopup', flowDiagramPhysicalFlowPopup)
        .component('waltzFlowDiagramAnnotationPopup', flowDiagramAnnotationPopup)
        .component('waltzFlowDiagramEditor', flowDiagramEditor)
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
        FlowDiagramsPanelBrowse,
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
