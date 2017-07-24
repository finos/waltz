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


import * as flowDiagramStore from './services/flow-diagram-store';
import * as flowDiagramAnnotationStore from './services/flow-diagram-annotation-store';
import * as flowDiagramEntityStore from './services/flow-diagram-entity-store';

import flowDiagramStateService from './services/flow-diagram-state-service';

import flowDiagram from './components/diagram/flow-diagram';
import flowDiagramLogicalFlowPopup from './components/editor/flow-diagram-logical-flow-popup';
import flowDiagramNodePopup from './components/editor/flow-diagram-node-popup';
import flowDiagramPhysicalFlowPopup from './components/editor/flow-diagram-physical-flow-popup';
import flowDiagramAnnotationPopup from './components/editor/flow-diagram-annotation-popup';
import flowDiagramEditor from './components/editor/flow-diagram-editor';
import flowDiagramsPanel from './components/diagrams-panel/flow-diagrams-panel';
import flowDiagramsSection from './components/section/flow-diagrams-section';
import * as flowDiagramAssociations from './components/associations/flow-diagram-associations';

import {registerComponents, registerStores} from '../common/module-utils'

import routes from './routes';


export default () => {

    const module = angular.module('waltz.flow-diagram', []);

    module
        .service('FlowDiagramStateService', flowDiagramStateService)
        ;

    module
        .component('waltzFlowDiagram', flowDiagram)
        .component('waltzFlowDiagramLogicalFlowPopup', flowDiagramLogicalFlowPopup)
        .component('waltzFlowDiagramNodePopup', flowDiagramNodePopup)
        .component('waltzFlowDiagramPhysicalFlowPopup', flowDiagramPhysicalFlowPopup)
        .component('waltzFlowDiagramAnnotationPopup', flowDiagramAnnotationPopup)
        .component('waltzFlowDiagramEditor', flowDiagramEditor)
        .component('waltzFlowDiagramsPanel', flowDiagramsPanel)
        .component('waltzFlowDiagramsSection', flowDiagramsSection)
        ;

    module
        .config(routes)
        ;

    registerComponents(module, [
        flowDiagramAssociations
    ]);


    registerStores(module, [
        flowDiagramStore,
        flowDiagramAnnotationStore,
        flowDiagramEntityStore
    ]);


    return module.name;
};
