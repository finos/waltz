/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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


import * as FlowDiagramStore from './services/flow-diagram-store';
import * as FlowDiagramAnnotationStore from './services/flow-diagram-annotation-store';
import * as FlowDiagramEntityStore from './services/flow-diagram-entity-store';

import * as FlowDiagramStarterService from './services/flow-diagram-starter-service';
import * as FlowDiagramStateService from './services/flow-diagram-state-service';

import flowDiagram from './components/diagram/flow-diagram';
import flowDiagramLogicalFlowPopup from './components/editor/flow-diagram-logical-flow-popup';
import flowDiagramNodePopup from './components/editor/flow-diagram-node-popup';
import flowDiagramPhysicalFlowPopup from './components/editor/flow-diagram-physical-flow-popup';
import flowDiagramAnnotationPopup from './components/editor/flow-diagram-annotation-popup';
import flowDiagramEditor from './components/editor/flow-diagram-editor';
import FlowDiagramsPanel from './components/diagrams-panel/flow-diagrams-panel';
import FlowDiagramsPanelBrowse from './components/diagrams-panel/browse/flow-diagrams-panel-browse';
import FlowDiagramsPanelView from './components/diagrams-panel/view/flow-diagrams-panel-view';
import flowDiagramsSection from './components/section/flow-diagrams-section';
import FlowDiagramAssociations from './components/associations/flow-diagram-associations';

import {registerComponents, registerServices, registerStores} from '../common/module-utils'

import routes from './routes';


export default () => {

    const module = angular.module('waltz.flow-diagram', []);

    module
        .component('waltzFlowDiagram', flowDiagram)
        .component('waltzFlowDiagramLogicalFlowPopup', flowDiagramLogicalFlowPopup)
        .component('waltzFlowDiagramNodePopup', flowDiagramNodePopup)
        .component('waltzFlowDiagramPhysicalFlowPopup', flowDiagramPhysicalFlowPopup)
        .component('waltzFlowDiagramAnnotationPopup', flowDiagramAnnotationPopup)
        .component('waltzFlowDiagramEditor', flowDiagramEditor)
        .component('waltzFlowDiagramsSection', flowDiagramsSection)
        ;

    module
        .config(routes)
        ;

    registerServices(module, [
        FlowDiagramStarterService,
        FlowDiagramStateService
    ]);

    registerComponents(module, [
        FlowDiagramAssociations,
        FlowDiagramsPanel,
        FlowDiagramsPanelBrowse,
        FlowDiagramsPanelView
    ]);

    registerStores(module, [
        FlowDiagramStore,
        FlowDiagramAnnotationStore,
        FlowDiagramEntityStore
    ]);

    return module.name;
};
