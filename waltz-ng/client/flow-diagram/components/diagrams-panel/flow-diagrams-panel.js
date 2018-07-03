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

import {initialiseData} from "../../../common";


import template from './flow-diagrams-panel.html';
import {toEntityRef} from "../../../common/entity-utils";


const bindings = {
    parentEntityRef: '<',
    canCreate: '<'
};


const initialState = {
    flowDiagrams: [],
    flowDiagramEntities: [],
    diagrams: [],
    visibility: {
        mode: 'BROWSE',
        diagram: false,
        editor: false
    },
    selected: {
        diagram: null,
        node: null,
        flowBucket: null
    }
};


function controller(
    serviceBroker,
    flowDiagramStarterService,
    flowDiagramStateService,
    notification)
{
    const vm = initialiseData(this, initialState);

    const showReadOnlyDiagram = () => {
        vm.visibility.mode = 'VIEW';
    };

    const showEditableDiagram = () => {
        vm.visibility.mode = 'EDIT';
    };


    const selectDiagram = (diagram) => {
        vm.selected.diagram = diagram;
        vm.selected.diagramRef = toEntityRef(diagram, 'FLOW_DIAGRAM');
    };

    vm.onSelectDiagram = (diagram) => {
        selectDiagram(diagram);
        showReadOnlyDiagram();
    };

    vm.onDiagramDismiss = () => {
        vm.visibility.mode = 'BROWSE';
        flowDiagramStateService.reset();
    };

    vm.onCreateDiagram = () => {
        flowDiagramStateService
            .reset();

        flowDiagramStarterService
            .mkCommands(vm.parentEntityRef)
            .then(starterCommands => {
                showEditableDiagram();
                flowDiagramStateService.processCommands(starterCommands);
                notification.warning("Flow diagrams are not automatically saved. Remember to save your work.")
            });
    };

    vm.contextMenus = {};


    vm.onEditDiagram = (diagram) => {

        selectDiagram(diagram);
        flowDiagramStateService.reset();
        flowDiagramStateService
            .load(diagram.id)
            .then(() => {
                flowDiagramStateService.processCommands([
                    { command: 'SET_TITLE', payload: diagram.name }
                ]);
                showEditableDiagram();
                notification.warning("Flow diagrams are not automatically saved.  Remember to save your work.");
            });
    };

    vm.onCloneDiagram = (diagram) => {
        vm.selected.diagram = diagram;
        flowDiagramStateService.reset();
        flowDiagramStateService
            .load(diagram.id)
            .then(() => {
                flowDiagramStateService.processCommands([
                    { command: 'CLONE', payload: 'Copy of ' + vm.selected.diagram.name }
                ]);
                showEditableDiagram();
                notification.warning("Flow diagrams are not automatically saved.  Remember to save your work.")
                notification.warning("You have cloned an existing diagram.  Remember to change it's name.")
            });
    };

}


controller.$inject = [
    'ServiceBroker',
    'FlowDiagramStarterService',
    'FlowDiagramStateService',
    'Notification'
];


const component = {
    template,
    bindings,
    controller
};


export default {
    id: 'waltzFlowDiagramsPanel2',
    component
};